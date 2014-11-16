package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Loaders.AlbumsLoader;
import ru.example.PhotoStream.Tasks.GetCurrentUserTask;
import ru.example.PhotoStream.Tasks.GetGroupsTask;
import ru.example.PhotoStream.Tasks.GetUsersTask;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;
import java.util.concurrent.*;

public class FeedsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, View.OnLayoutChangeListener {

    private class GroupsLoading implements Callable<List<Group>> {

        protected Odnoklassniki api;

        public GroupsLoading(Odnoklassniki api) {
            this.api = api;
        }

        @Override
        public List<Group> call() throws Exception {
            List<String> uids = getGroupIds();
            List<Group> groups = new GetGroupsTask(api, uids.toArray(new String[uids.size()])).call();
            return groups;
        }

        protected List<String> getGroupIds() {
            Map<String, String> requestParams = new HashMap<>();
            List<String> result = new ArrayList<>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject groupsObject = new JSONObject(api.request("group.getUserGroupsV2", requestParams, "get"));
                    if (groupsObject.isNull("groups")) {
                        hasMore = false;
                    } else {
                        JSONArray groups = groupsObject.getJSONArray("groups");
                        for (int i = 0; i < groups.length(); ++i) {
                            result.add(groups.getJSONObject(i).getString("groupId"));
                        }
                        requestParams.put("anchor", groupsObject.getString("anchor"));
                    }
                } catch (Exception e) {
                    Console.print(e.getMessage());
                    hasMore = false;
                }
            }
            return result;
        }
    }

    private class FriendsLoading implements Callable<List<User>> {

        protected Odnoklassniki api;

        public FriendsLoading(Odnoklassniki api) {
            this.api = api;
        }

        @Override
        public List<User> call() throws Exception {
            List<String> uids = getFriendIDs();
            return new GetUsersTask(api, uids.toArray(new String[uids.size()])).call();
        }

        protected List<String> getFriendIDs() {
            List<String> result = new ArrayList<>();
            try {
                JSONArray friendIDs = new JSONArray(api.request("friends.get", null, "get"));
                for (int i = 0; i < friendIDs.length(); i++) {
                    result.add(friendIDs.getString(i));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
            return result;
        }
    }

    private Odnoklassniki api;
    private ArrayAdapter<AlbumsOwner> feedsAdapter;
    private static List<AlbumsOwner> albumsOwners;
    private ConcurrentHashMap<AlbumsOwner, Photo> photos;
    private static Map<AlbumsOwner, Feed> feeds;
    private static Map<AlbumsOwner, PhotoShifter> photoShifters;
    private static Map<AlbumsOwner, IEventHandler> listeners;
    private GridView feedsGrid;
    private int targetSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UpdateManager.register(this, "5adb6faead01ccaa24e6865215ddcb59");
        lockOrientation();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.feedsactivity);
        api = Odnoklassniki.getInstance(this);
        photos = new ConcurrentHashMap<>();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        targetSize = size.x / 3;
        feedsAdapter = new ArrayAdapter<AlbumsOwner>(this, R.layout.badgeview) {
            private ConcurrentHashMap<View, Integer> lastPosition = new ConcurrentHashMap<>();

            class ViewHolder {
                TextView title;
                SmartImage image;

                ViewHolder(TextView title, SmartImage image) {
                    this.title = title;
                    this.image = image;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AlbumsOwner owner = getItem(position);
                ViewHolder viewHolder;
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.badgeview, parent, false);
                    viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.badgeview_title),
                            (SmartImage)convertView.findViewById(R.id.badgeview_image));
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                boolean sameView = lastPosition.containsKey(convertView) && (lastPosition.get(convertView) == position);
                lastPosition.put(convertView, position);
                Photo photo = photos.get(owner);
                if (photo != null && photo.hasAnySize()) {
                    String url = photo.findBestSize(targetSize, targetSize).getUrl();
                    if (!sameView) {
                        viewHolder.image.setAsFirstCalled();
                    }
                    viewHolder.image.loadFromURL(url);
                }
                viewHolder.title.setText(owner.getName());
                return convertView;
            }
        };

        feedsGrid = (GridView) findViewById(R.id.feedsactivity_grid);
        feedsGrid.setOnItemClickListener(this);
        feedsGrid.addOnLayoutChangeListener(this);
        feedsGrid.setAdapter(feedsAdapter);
        if (savedInstanceState == null || !savedInstanceState.containsKey("orientationChanged")) {
            albumsOwners = new ArrayList<>();
            feeds = new HashMap<>();
            photoShifters = new HashMap<>();
            listeners = new HashMap<>();
            MultiTask<String> executor = new MultiTask<String>() {
                @Override
                protected void onPostExecute(Map<String, Future<?>> data) {
                    Future<List<User>> futureUsers = (Future<List<User>>) data.get("friends");
                    Future<List<Group>> futureGroups = (Future<List<Group>>) data.get("groups");
                    Future<User> futureCurrentUser = (Future<User>) data.get("current");
                    try {
                        User currentUser = futureCurrentUser.get();
                        User.currentUID = currentUser.getId();
                        List<Group> groups = futureGroups.get();
                        List<User> users = futureUsers.get();

                        albumsOwners.add(currentUser);
                        albumsOwners.addAll(groups);
                        albumsOwners.addAll(users);
                        MultiTask<AlbumsOwner> albumLoader = new MultiTask<AlbumsOwner>() {
                            @Override
                            protected void onPostExecute(Map<AlbumsOwner, Future<?>> data) {
                                for (final AlbumsOwner albumsOwner: data.keySet()) {
                                    Future<List<Album>> future = (Future<List<Album>>) data.get(albumsOwner);
                                    try {
                                        List<Album> albums = future.get();
                                        Feed feed;
                                        if (albumsOwner instanceof Group) {
                                            feed = new LineFeed(api);
                                        } else {
                                            feed = new SortedFilteredFeed(api);
                                        }
                                        feeds.put(albumsOwner, feed);
                                        final int position = feeds.size() - 1;
                                        feed.addAll(albums);
                                        PhotoShifter photoShifter = new PhotoShifter(feed);
                                        photoShifters.put(albumsOwner, photoShifter);
                                        IEventHandler handler = new IEventHandler() {
                                            @Override
                                            public void handleEvent(Event e) {
                                                Photo photo = (Photo)e.data.get("photo");
                                                photos.put(albumsOwner, photo);
                                                FeedsActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (position >= feedsGrid.getFirstVisiblePosition()
                                                            && position <= feedsGrid.getLastVisiblePosition()) {
                                                            feedsAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                            }
                                        };
                                        photoShifter.addEventListener(handler);
                                        listeners.put(albumsOwner, handler);
                                        photoShifter.immediateGet();
                                    } catch (Exception e) {
                                        Log.d("M_CONSOLE", e.getMessage(), e);
                                    }
                                }
                            }
                        };
                        for (AlbumsOwner albumsOwner: albumsOwners) {
                            photos.put(albumsOwner, Photo.get(albumsOwner.getAvatarId()));
                            albumLoader.put(albumsOwner, new AlbumsLoader(api, albumsOwner));
                        }
                        albumLoader.execute();
                        feedsAdapter.addAll(albumsOwners);
                    } catch (Exception e) {
                        Log.d("M_CONSOLE", e.getMessage(), e);
                    }
                    feedsAdapter.notifyDataSetChanged();
                    unlockOrientation();
                }
            };
            executor.put("friends", new FriendsLoading(api));
            executor.put("groups", new GroupsLoading(api));
            executor.put("current", new GetCurrentUserTask(api));
            executor.execute();
        } else {
            for (final AlbumsOwner albumsOwner: albumsOwners) {
                PhotoShifter photoShifter = photoShifters.get(albumsOwner);
                photoShifter.removeEventListener(listeners.get(albumsOwner));
                IEventHandler handler = new IEventHandler() {
                    @Override
                    public void handleEvent(Event e) {
                        Photo photo = (Photo)e.data.get("photo");
                        photos.put(albumsOwner, photo);
                        FeedsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                feedsAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                };
                listeners.put(albumsOwner, handler);
                photoShifter.addEventListener(handler);
                photoShifter.immediateGet();
            }
            feedsAdapter.addAll(albumsOwners);
            feedsAdapter.notifyDataSetChanged();
            unlockOrientation();
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;
        int currentWidth = right - left;
        int currentHeight = bottom - top;
        if (oldWidth != currentWidth || oldHeight != currentHeight) {
            targetSize = currentWidth / feedsGrid.getNumColumns();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload: {
                SmartImage.clearCache();
                Intent intent = new Intent(this, PhotoTakerActivity.class);
                startActivity(intent);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashManager.register(this, "5adb6faead01ccaa24e6865215ddcb59");
        for (PhotoShifter photoShifter : photoShifters.values()) {
            photoShifter.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (PhotoShifter photoShifter: photoShifters.values()) {
            photoShifter.pause();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlbumsOwner albumsOwner = feedsAdapter.getItem(position);
        try {
            int pos = photoShifters.get(albumsOwner).getPosition();
            PhotoActivity.setFeed(feeds.get(albumsOwner));
            SmartImage.clearCache();
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("position", pos);
            startActivity(intent);
        } catch (Exception e) {
            Log.d("CONSOLE", e.getMessage(), e);
            Console.print(albumsOwner);
            Console.print(albumsOwner.getName());
            Console.print(photoShifters.size());
            Console.print(photoShifters.toString());
        }
    }

    private void lockOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        saveState.putBoolean("orientationChanged", true);
    }
}

package ru.example.PhotoStream.Activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;
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
    private ArrayAdapter<AlbumsOwner> feeds;
    private static Map<AlbumsOwner, Photo> currentPhotos = new HashMap<>();
    private static Map<AlbumsOwner, Feed> currentFeeds = new HashMap<>();
    private static Map<AlbumsOwner, PhotoShifter> photoShifters = new HashMap<>();
    private GridView feedsGrid;
    private int targetSize;

    private synchronized Photo getAlbumsOwnerPhoto(AlbumsOwner albumsOwner) {
        return currentPhotos.get(albumsOwner);
    }

    private synchronized void setAlbumsOwnerPhoto(AlbumsOwner albumsOwner, Photo photo) {
        currentPhotos.put(albumsOwner, photo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedsactivity);
        api = Odnoklassniki.getInstance(this);

        feeds = new ArrayAdapter<AlbumsOwner>(this, R.layout.badgeview) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AlbumsOwner owner = getItem(position);
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.badgeview, parent, false);
                SmartImage image = (SmartImage) view.findViewById(R.id.badgeview_image);
                Photo photo = getAlbumsOwnerPhoto(owner);
                if (photo.hasAnySize()) {
                    image.loadFromURL(photo.findBestSize(targetSize, targetSize).getUrl());
                }

                TextView title = (TextView) view.findViewById(R.id.badgeview_title);
                title.setText(owner.getName());
                return view;
            }
        };
        feedsGrid = (GridView) findViewById(R.id.feedsactivity_grid);

        feedsGrid.setOnItemClickListener(this);
        feedsGrid.addOnLayoutChangeListener(this);
        feedsGrid.setAdapter(feeds);

        MultiTask<String> executor = new MultiTask<String>() {
            @Override
            protected void onPostExecute(Map<String, Future<?>> data) {
                feeds.clear();
                Future<List<User>> futureUsers = (Future<List<User>>) data.get("friends");
                Future<List<Group>> futureGroups = (Future<List<Group>>) data.get("groups");
                Future<User> futureCurrentUser = (Future<User>) data.get("current");
                try {
                    User currentUser = futureCurrentUser.get();
                    User.currentUID = currentUser.getId();
                    List<Group> groups = futureGroups.get();
                    List<User> users = futureUsers.get();

                    List<AlbumsOwner> albumsOwners = new ArrayList<>();
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
                                    Feed feed = new LineFeed(api);
                                    currentFeeds.put(albumsOwner, feed);
                                    feed.addAll(albums);
                                    PhotoShifter photoShifter = new PhotoShifter(feed);
                                    photoShifters.put(albumsOwner, photoShifter);
                                    photoShifter.addEventListener(new IEventHandler() {
                                        @Override
                                        public void handleEvent(Event e) {
                                            Photo photo = (Photo)e.data.get("photo");
                                            setAlbumsOwnerPhoto(albumsOwner, photo);
                                            FeedsActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    feeds.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.d("M_CONSOLE", e.getMessage(), e);
                                }
                            }
                        }
                    };
                    for (AlbumsOwner albumsOwner: albumsOwners) {
                        if (!currentFeeds.containsKey(albumsOwner)) {
                            setAlbumsOwnerPhoto(albumsOwner, Photo.get(albumsOwner.getAvatarId()));
                            albumLoader.put(albumsOwner, new AlbumsLoader(api, albumsOwner));
                        }
                    }
                    albumLoader.execute();
                    feeds.addAll(albumsOwners);
                    Set<AlbumsOwner> previous = currentFeeds.keySet();
                    previous.removeAll(albumsOwners);
                    feeds.addAll(previous);
                } catch (Exception e) {
                    Log.d("M_CONSOLE", e.getMessage(), e);
                }

                feeds.notifyDataSetChanged();
            }
        };
        executor.put("friends", new FriendsLoading(api));
        executor.put("groups", new GroupsLoading(api));
        executor.put("current", new GetCurrentUserTask(api));
        executor.execute();
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
        for (PhotoShifter photoShifter: photoShifters.values()) {
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
        AlbumsOwner albumsOwner = feeds.getItem(position);
        try {
            int pos = photoShifters.get(albumsOwner).getPosition();
            if (pos == -1) {
                pos = 0;
            }
            PhotoActivity.setFeed(currentFeeds.get(albumsOwner));
            Intent intent = new Intent(this, StreamActivity.class);
            if (albumsOwner instanceof User) {
                //Console.print("FID: " + albumsOwner.getName() + " " + albumsOwner.getId());
                intent.putExtra("fid", albumsOwner.getId());
            } else if (albumsOwner instanceof Group) {
                intent.putExtra("gid", albumsOwner.getId());
            }
            intent.putExtra("position", pos);
            StreamActivity.setForwarding(true);
            startActivity(intent);
        } catch (Exception e) {
            Log.d("CONSOLE", e.getMessage(), e);
            Console.print(albumsOwner);
            Console.print(albumsOwner.getName());
            Console.print(photoShifters.size());
            Console.print(photoShifters.toString());
        }
        finally {

        }
    }
}

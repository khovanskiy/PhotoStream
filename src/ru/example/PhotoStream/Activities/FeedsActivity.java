package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import net.hockeyapp.android.CrashManager;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Loaders.AlbumsLoader;
import ru.example.PhotoStream.Tasks.GetCurrentUserTask;
import ru.example.PhotoStream.Tasks.GetGroupsTask;
import ru.example.PhotoStream.Tasks.GetUsersTask;
import ru.ok.android.sdk.Odnoklassniki;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

public class FeedsActivity extends UIActivity implements AdapterView.OnItemClickListener, IEventHandler {

    @Override
    public void handleEvent(Event e) {
        if (e.type == FeedPreview.EVENT_UPDATED) {
            FeedsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    feedsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

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

    private class FeedsAdapter extends ArrayAdapter<FeedPreview> {
        private ConcurrentHashMap<View, Integer> lastPosition = new ConcurrentHashMap<>();

        private class ViewHolder {
            public final TextView title;
            public final SmartImage image;

            public ViewHolder(TextView title, SmartImage image) {
                this.title = title;
                this.image = image;
            }
        }

        public FeedsAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeedPreview preview = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.badgeview, parent, false);
                viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.badgeview_title),
                        (SmartImage) convertView.findViewById(R.id.badgeview_image));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            boolean sameView = lastPosition.containsKey(convertView) && (lastPosition.get(convertView) == position);
            lastPosition.put(convertView, position);

            Photo photo = preview.getPhoto();
            if (photo != null && photo.hasAnySize()) {
                String url = photo.findBestSize(targetSize, targetSize).getUrl();
                if (!sameView) {
                    viewHolder.image.setAsFirstCalled();
                }
                viewHolder.image.loadFromURL(url);
            }
            //viewHolder.title.setText(owner.getName());
            return convertView;
        }
    }

    private class FeedsUpdater extends MultiTask<String> {

        private WeakReference<FeedsActivity> activityWeakReference = null;
        private Odnoklassniki api;

        public FeedsUpdater(Odnoklassniki api, FeedsActivity feedsActivity) {
            this.activityWeakReference = new WeakReference<>(feedsActivity);
            this.api = api;
        }

        private class AlbumUpdater extends MultiTask<AlbumsOwner> {
            @Override
            protected void onPostExecute(Map<AlbumsOwner, Object> data) {
                Console.print("albums loader onPost");
                FeedsActivity feedsActivity = activityWeakReference.get();
                if (feedsActivity == null) {
                    return;
                }
                for (AlbumsOwner albumsOwner : data.keySet()) {
                    createFeed(feedsActivity, albumsOwner);
                }
                feedsActivity.feedsAdapter.notifyDataSetChanged();
            }
        }

        protected void createFeed(FeedsActivity feedsActivity, AlbumsOwner albumsOwner) {
            Feed feed = new LineFeed(api);
            feed.addAll(albumsOwner.getAlbums());
            FeedPreview feedPreview = new PhotoShifter(feed, albumsOwner.getAvatar());
            feedsActivity.feedsAdapter.add(feedPreview);
            feedsActivity.feedPreviews.add(feedPreview);
            feedPreview.addEventListener(feedsActivity);
            feedPreview.start();
        }

        @Override
        protected void onPreExecute() {
            this.put("friends", new FriendsLoading(api));
            this.put("groups", new GroupsLoading(api));
            this.put("current", new GetCurrentUserTask(api));
        }

        @Override
        protected void onPostExecute(Map<String, Object> data) {
            FeedsActivity feedsActivity = activityWeakReference.get();
            if (feedsActivity == null) {
                return;
            }

            feedsActivity.currentUser = (User) data.get("current");
            User.currentUID = feedsActivity.currentUser.getId();
            feedsActivity.groups = (List<Group>) data.get("groups");
            feedsActivity.friends = (List<User>) data.get("friends");

            List<AlbumsOwner> albumsOwners = new LinkedList<>();
            albumsOwners.add(feedsActivity.currentUser);
            albumsOwners.addAll(feedsActivity.groups);
            albumsOwners.addAll(feedsActivity.friends);
            Console.print("Friends = " + feedsActivity.friends.size() + " Groups = " + groups.size());
            MultiTask<AlbumsOwner> albumLoader = new AlbumUpdater();
            for (AlbumsOwner albumsOwner : albumsOwners) {
                //if (User.get(albumsOwner.getId()) == null && Group.get(albumsOwner.getId()) == null) {
                    albumLoader.put(albumsOwner, new AlbumsLoader(api, albumsOwner));
                //} else {
                  //  createFeed(feedsActivity, albumsOwner);
                //}
            }
            albumLoader.execute();
        }
    }

    private final static String ARG_CURRENT_USER = "currentUserLabel";
    private final static String USER_FRIENDS_LABEL = "userFriendsLabel";
    private final static String USER_GROUPS_LABEL = "userGroupsLabel";

    //private Odnoklassniki api;
    private ArrayAdapter<FeedPreview> feedsAdapter;
    private List<FeedPreview> feedPreviews = new ArrayList<>();
    private AlbumsOwner currentUser;
    private List<User> friends;
    private List<Group> groups;

    //private HashSet<AlbumsOwner> cached;

    private GridView feedsGrid;
    private int targetSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Console.print("OnCreate");
        super.onCreate(savedInstanceState);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.feedsactivity);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        targetSize = size.x / 3;

        feedsAdapter = new FeedsAdapter(this, R.layout.badgeview);
        feedsGrid = (GridView) findViewById(R.id.feedsactivity_grid);
        feedsGrid.setOnItemClickListener(this);
        feedsGrid.setAdapter(feedsAdapter);

        groups = (List<Group>) UIActivity.instance(FeedsActivity.class).getParam(USER_GROUPS_LABEL);
        friends = (List<User>) UIActivity.instance(FeedsActivity.class).getParam(USER_FRIENDS_LABEL);
        currentUser = (User) UIActivity.instance(FeedsActivity.class).getParam(ARG_CURRENT_USER);

        if (currentUser == null) {
            Console.print("Execute feeds updater");
            new FeedsUpdater(Odnoklassniki.getInstance(this), this).execute();
        }
        //if (savedInstanceState == null || !savedInstanceState.containsKey("orientationChanged")) {
        //albumsOwners = new ArrayList<>();
        //feeds = new HashMap<>();
        //photoShifters = new HashMap<>();
        //listeners = new HashMap<>();
        //Console.print("FeedsUpdater.execute()");
        //new FeedsUpdater(this).execute();
        //} else {
            /*for (final AlbumsOwner albumsOwner : albumsOwners) {
                PhotoShifter photoShifter = photoShifters.get(albumsOwner);
                photoShifter.removeEventListener(listeners.get(albumsOwner));
                IEventHandler handler = new IEventHandler() {
                    @Override
                    public void handleEvent(Event e) {
                        Photo photo = (Photo) e.data.get("photo");
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
            unlockOrientation();*/
        //}
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
        Console.print("onResume");
        super.onResume();
        CrashManager.register(this, "5adb6faead01ccaa24e6865215ddcb59");
        for (FeedPreview feedPreview : feedPreviews) {
            feedPreview.start();
        }
    }

    @Override
    protected void onPause() {
        Console.print("OnPause");
        super.onPause();
        for (FeedPreview feedPreview : feedPreviews) {
            feedPreview.pause();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FeedPreview photoShifter = feedsAdapter.getItem(position);
        int pos = photoShifter.getPosition();
        PhotoActivity.setFeed(photoShifter.getFeed());
        Intent intent = new Intent(this, PhotoActivity.class);
        UIActivity.instance(PhotoActivity.class).putParam("position", pos);

        intent.putExtra("position", pos);
        startActivity(intent);
    }

    /*private void lockOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }*/

    @Override
    protected void onDestroy() {
        Console.print("onDestroy");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Console.print("Save instance state");
        UIActivity.instance(FeedsActivity.class).putParam(ARG_CURRENT_USER, currentUser);
        UIActivity.instance(FeedsActivity.class).putParam(USER_FRIENDS_LABEL, friends);
        UIActivity.instance(FeedsActivity.class).putParam(USER_GROUPS_LABEL, groups);
        super.onSaveInstanceState(savedInstanceState);
    }
}

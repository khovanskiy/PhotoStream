package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.util.*;
import java.util.concurrent.*;

import ru.example.PhotoStream.*;

public class FeedsActivity extends UIActivity implements AdapterView.OnItemClickListener {

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
                    //viewHolder.image.setAsFirstCalled();
                }
                viewHolder.image.setImageURL(url);
            }
            viewHolder.title.setText(preview.getFeed().getName());
            return convertView;
        }
    }

    private ArrayAdapter<FeedPreview> feedsAdapter;
    private List<FeedPreview> feedPreviews = new ArrayList<>();

    private User mCurrentUser;
    private List<User> mCurrentFriends;
    private List<Group> mCurrentGroups;

    private GridView feedsGrid;
    private int targetSize;

    private OKRequest friendsRequest;
    private OKRequest groupsRequest;
    private OKBatchRequest feedsRequest;

    private final IEventHandler previewUpdatedHandler = new IEventHandler() {
        @Override
        public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
            feedsAdapter.notifyDataSetChanged();
        }
    };

    private final OKBatchRequest.OKBatchRequestListener feedsListener = new OKBatchRequest.OKBatchRequestListener() {
        @Override
        public void onComplete(OKResponse[] responses) {
            List<User> usersDiff = Collections.emptyList();
            List<Group> groupsDiff = Collections.emptyList();
            for (OKResponse response : responses) {
                if (response.request == friendsRequest) {
                    List<User> users = (List<User>) response.parsedModel;
                    weakCache(FeedsActivity.class).put("friends", users);
                    usersDiff = getDifference(users, mCurrentFriends);
                    mCurrentFriends = users;
                } else if (response.request == groupsRequest) {
                    List<Group> groups = (List<Group>) response.parsedModel;
                    weakCache(FeedsActivity.class).put("groups", groups);
                    groupsDiff = getDifference(groups, mCurrentGroups);
                    mCurrentGroups = groups;
                }
            }
            updateOwnerAlbums(usersDiff, groupsDiff);
        }
    };

    private final OKRequest.OKRequestListener currentUserListener = new OKRequest.OKRequestListener() {
        @Override
        public void onComplete(OKResponse response) {
            User currentUser = (User) response.parsedModel;
            weakCache(FeedsActivity.class).put("currentUser", currentUser);
            mCurrentUser = currentUser;
            fetchNewFeeds();
        }
    };

    private <T> List<T> getDifference(List<T> newItems, List<T> oldItems) {
        if (oldItems == null) {
            return newItems;
        }
        // Fast but not correct
        if (newItems.size() == oldItems.size()) {
            return Collections.emptyList();
        }
        List<T> difference = new ArrayList<T>();
        for (T a : newItems) {
            boolean found = false;
            for (T b : oldItems) {
                if (a.equals(b)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.add(a);
            }
        }
        return null;
    }

    private void fetchNewFeeds() {
        if (mCurrentUser == null) {
            return;
        }
        friendsRequest = OKApi.friends().get(OKParameters.from("fid", mCurrentUser.getId(), "fields", "uid, first_name, last_name, name, photo_id"));
        groupsRequest = OKApi.groups().get(OKParameters.from("fid", mCurrentUser.getId(), "fields", "group.*"));
        feedsRequest = new OKBatchRequest(friendsRequest, groupsRequest);
        feedsRequest.executeWithListener(feedsListener);
    }

    private void updateFeedPreviews() {
        final User currentUser = mCurrentUser;
        final List<User> friends = mCurrentFriends;
        final List<Group> groups = mCurrentGroups;
        List<AlbumsOwner> albumsOwners = new ArrayList<>(1 + friends.size() + groups.size());
        albumsOwners.add(currentUser);
        albumsOwners.addAll(friends);
        albumsOwners.addAll(groups);
        feedsAdapter.clear();
        feedPreviews.clear();
        for (AlbumsOwner albumsOwner : albumsOwners) {
            Feed feed = new LineFeed(albumsOwner.getName());
            feed.addAll(albumsOwner.getAlbums());
            //System.out.println(albumsOwner.getName() + " " + albumsOwner.getAlbums().size());
            FeedPreview feedPreview = new PhotoShifter(feed, albumsOwner.getAvatar());
            feedsAdapter.add(feedPreview);
            feedPreviews.add(feedPreview);
            feedPreview.addEventListener(previewUpdatedHandler);
            feedPreview.start();
        }
        feedsAdapter.notifyDataSetChanged();
    }

    private void updateOwnerAlbums(final List<User> usersDiff, final List<Group> groupsDiff) {
        if (usersDiff.size() == 0 && groupsDiff.size() == 0) {
            updateFeedPreviews();
            return;
        }
        OKRequest[] requests = new OKRequest[1 + usersDiff.size() + groupsDiff.size()];
        requests[0] = OKApi.albums().get(mCurrentUser, OKParameters.from("fields", "user_album.*"));
        for (int i = 0; i < usersDiff.size(); ++i) {
            User user = mCurrentFriends.get(i);
            requests[i + 1] = OKApi.albums().get(user, OKParameters.from("fields", "user_album.*"));
        }
        for (int i = 0; i < groupsDiff.size(); ++i) {
            Group group = mCurrentGroups.get(i);
            requests[usersDiff.size() + i + 1] = OKApi.albums().get(group, OKParameters.from("fields", "group_album.*"));
        }

        OKBatchRequest albumsRequest = new OKBatchRequest(requests);
        albumsRequest.executeWithListener(new OKBatchRequest.OKBatchRequestListener() {
            @Override
            public void onComplete(OKResponse[] responses) {
                mCurrentUser.setAlbums((List<Album>) responses[0].parsedModel);
                for (int i = 0; i < usersDiff.size(); ++i) {
                    usersDiff.get(i).setAlbums((List<Album>) responses[i + 1].parsedModel);
                }
                for (int i = 0; i < groupsDiff.size(); ++i) {
                    groupsDiff.get(i).setAlbums((List<Album>) responses[usersDiff.size() + i + 1].parsedModel);
                }
                updateFeedPreviews();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.feedsactivity);
        //System.out.println("onCreate");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        targetSize = size.x / 3;

        feedsAdapter = new FeedsAdapter(this, R.layout.badgeview);
        feedsGrid = (GridView) findViewById(R.id.feedsactivity_grid);
        feedsGrid.setOnItemClickListener(this);
        feedsGrid.setAdapter(feedsAdapter);

        mCurrentFriends = (List<User>) weakCache(FeedsActivity.class).get("friends");
        mCurrentGroups = (List<Group>) weakCache(FeedsActivity.class).get("groups");
        if (weakCache(FeedsActivity.class).containsKey("currentUser")) {
            mCurrentUser = (User) weakCache(FeedsActivity.class).get("currentUser");
        } else {
            OKApi.users().get().executeWithListener(currentUserListener);
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
        //System.out.println("onResume");
        fetchNewFeeds();
        for (FeedPreview feedPreview : feedPreviews) {
            feedPreview.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //System.out.println("onPause");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //System.out.println("onDestroy");
    }
}

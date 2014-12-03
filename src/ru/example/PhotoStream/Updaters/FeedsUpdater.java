package ru.example.PhotoStream.Updaters;

import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Activities.FeedsActivity;
import ru.example.PhotoStream.Loaders.AlbumsLoader;
import ru.example.PhotoStream.Tasks.GetCurrentUserTask;
import ru.ok.android.sdk.Odnoklassniki;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeedsUpdater extends MultiTask<String> {

    private WeakReference<FeedsActivity> activityWeakReference = null;
    private Odnoklassniki api;

    public FeedsUpdater(Odnoklassniki api, FeedsActivity feedsActivity) {
        this.activityWeakReference = new WeakReference<>(feedsActivity);
        this.api = api;
    }

    private class AlbumUpdater extends MultiTask<AlbumsOwner> {
        @Override
        protected void onPostExecute(Map<AlbumsOwner, Object> data) {
            FeedsActivity feedsActivity = activityWeakReference.get();
            if (feedsActivity == null) {
                return;
            }

            for (AlbumsOwner albumsOwner : data.keySet()) {
                makeFeed(albumsOwner);
            }
            feedsActivity.feedsAdapter.notifyDataSetChanged();
        }
    }

    protected void makeFeed(AlbumsOwner albumsOwner) {
        Feed feed = new LineFeed(api);
        feed.addAll(albumsOwner.getAlbums());

        PhotoShifter photoShifter = new PhotoShifter(feed, albumsOwner.getAvatar());
        feedsActivity.feedsAdapter.add(photoShifter);

        /*photoShifter.addEventListener(new IEventHandler() {
            @Override
            public void handleEvent(Event e) {
                FeedsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        feedsAdapter.notifyDataSetChanged();
                    }
                });
            }
        });*/
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

        User currentUser = (User) data.get("current");
        User.currentUID = currentUser.getId();
        List<Group> groups = (List<Group>) data.get("groups");
        List<User> users = (List<User>) data.get("friends");

        List<AlbumsOwner> albumsOwners = new LinkedList<>();
        albumsOwners.add(currentUser);
        albumsOwners.addAll(groups);
        albumsOwners.addAll(users);

        MultiTask<AlbumsOwner> albumLoader = new AlbumUpdater();
        for (AlbumsOwner albumsOwner : albumsOwners) {
            if (User.get(albumsOwner.getId()) == null && Group.get(albumsOwner.getId()) == null) {
                albumLoader.put(albumsOwner, new AlbumsLoader(api, albumsOwner));
            } else {
                makeFeed(albumsOwner);
            }
        }
        albumLoader.execute();
    }
}

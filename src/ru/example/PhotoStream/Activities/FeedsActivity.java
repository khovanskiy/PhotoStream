package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Tasks.GetCurrentUserTask;
import ru.example.PhotoStream.Tasks.GetGroupsTask;
import ru.example.PhotoStream.Tasks.GetUsersTask;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FeedsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

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
            List<String> result = new ArrayList<String>();
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
    private GridView feedsGrid;

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
                String p = Photo.get(owner.getAvatarId()).pic50x50;
                image.loadFromURL(p);

                TextView title = (TextView) view.findViewById(R.id.badgeview_title);
                title.setText(owner.getName());
                return view;
            }
        };

        feedsGrid = (GridView) findViewById(R.id.feedsactivity_grid);
        feedsGrid.setOnItemClickListener(this);
        feedsGrid.setAdapter(feeds);

        MultiTask<String> executor = new MultiTask<String>() {
            @Override
            protected void onPostExecute(Map<String, Future<?>> data) {
                Console.print("onPostExecute");
                Future<List<User>> futureUsers = (Future<List<User>>) data.get("friends");
                Future<List<Group>> futureGroups = (Future<List<Group>>) data.get("groups");
                Future<User> futureCurrentUser = (Future<User>) data.get("current");
                try {
                    User currentUser = futureCurrentUser.get();
                    User.currentUID = currentUser.getId();
                    feeds.add(currentUser);

                    List<Group> groups = futureGroups.get();
                    feeds.addAll(groups);

                    List<User> users = futureUsers.get();
                    feeds.addAll(users);

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlbumsOwner albumsOwner = feeds.getItem(position);
        Intent intent = new Intent(this, StreamActivity.class);
        startActivity(intent);
    }
}

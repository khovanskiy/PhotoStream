package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import org.json.JSONObject;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Loaders.AlbumsLoader;
import ru.example.PhotoStream.Loaders.FriendsLoader;
import ru.example.PhotoStream.Loaders.GroupsLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitActivity extends ActionBarActivity implements IEventHadler{

    private class GetCurrentUserIdLoader extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                Map<String, String> requestParams = new HashMap<>();
                String response = api.request("users.getCurrentUser", requestParams, "get");
                JSONObject userObject = new JSONObject(response);
                String uid = userObject.getString("uid");
                return uid;
            } catch (Exception e) {
                Log.i("CONSOLE", "Error", e);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            User.currentUID = s;
            Intent intent = new Intent(InitActivity.this, StreamActivity.class);
            startActivity(intent);
        }
    }


    private Odnoklassniki api;
    private int semaphore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.please_stand_by);
        api = Odnoklassniki.getInstance(this);

        GroupsLoader groupsLoader = new GroupsLoader(api);
        groupsLoader.addEventListener(this);
        groupsLoader.execute();

        FriendsLoader friendsLoader = new FriendsLoader(api);
        friendsLoader.addEventListener(this);
        friendsLoader.execute();
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.GROUPS_LOADED) {
            e.target.removeEventListener(this);
            List<Group> groups = (List<Group>) e.data.get("groups");
            semaphore += groups.size();

            for (Group group : groups) {
                AlbumsLoader loader = new AlbumsLoader(api, group);
                loader.addEventListener(this);
                loader.execute();
            }
        } else if (e.type == Event.FRIENDS_LOADED) {
            e.target.removeEventListener(this);
            List<User> friends = (List<User>) e.data.get("friends");
            semaphore += friends.size() + 1;

            for (User user : friends) {
                AlbumsLoader loader = new AlbumsLoader(api, user);
                loader.addEventListener(this);
                loader.execute();
            }

            AlbumsLoader loader = new AlbumsLoader(api, User.get(""));
            loader.addEventListener(this);
            loader.execute();
        } else if (e.type == Event.ALBUMS_LOADED) {
            e.target.removeEventListener(this);

            if (semaphore > 0) {
                semaphore--;
            }

            if (semaphore == 0) {
                GetCurrentUserIdLoader loader = new GetCurrentUserIdLoader();
                loader.execute();
            }
        }
    }
}

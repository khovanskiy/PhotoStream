package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamActivity extends Activity
{
    private class FriendListAdapter extends BaseAdapter
    {
        private Context context;

        private class FriendInfo
        {
            String first_name, last_name;
            String user_id;
            View view;

            public FriendInfo(String user_id, String first_name, String last_name)
            {
                this.user_id = user_id;
                this.first_name = first_name;
                this.last_name = last_name;
                TextView textView = new TextView(context);
                textView.setText(last_name + " " + first_name);
                textView.setTextSize(20);
                this.view = textView;
            }
        }

        private List<FriendInfo> friendInfos;

        public FriendListAdapter(Context context)
        {
            this.context = context;
            this.friendInfos = new ArrayList<FriendInfo>();
        }

        public void addFriend(String user_id, String first_name, String last_name)
        {
            this.friendInfos.add(new FriendInfo(user_id, first_name, last_name));
            notifyDataSetChanged();
        }

        @Override
        public int getCount()
        {
            return friendInfos.size();
        }

        @Override
        public Object getItem(int position)
        {
            return friendInfos.get(position).view;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return friendInfos.get(position).view;
        }
    }

    private class FriendInfoLoader extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            try
            {
                JSONArray friendIDs = new JSONArray(TokenHolder.token.request("friends.get", null, "get"));
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < friendIDs.length(); i++)
                {
                    builder.append(',').append(friendIDs.getString(i));
                }
                Map<String, String> requestParams = new HashMap<String, String>();
                Console.print(builder.substring(1));
                requestParams.put("uids", builder.substring(1));
                requestParams.put("fields", "last_name, first_name");
                JSONArray friendInfos = new JSONArray(TokenHolder.token.request("users.getInfo", requestParams, "get"));
                Console.print(friendInfos.toString());
                JSONObject friend;
                for (int i = 0; i < friendIDs.length(); i++)
                {
                    friend = friendInfos.getJSONObject(i);
                    friendListAdapter.addFriend(friendIDs.getString(i), friend.getString("first_name"), friend.getString("last_name"));
                }
            }
            catch (Exception ignored)
            {
            }
            return null;
        }
    }

    private ListView friendList;
    private FriendListAdapter friendListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streamactivity);
        friendList = (ListView) findViewById(R.id.authactivity_friendlist);
    }

    private void update()
    {
        friendListAdapter = new FriendListAdapter(this);
        friendList.setAdapter(friendListAdapter);
        new FriendInfoLoader().execute();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        update();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }
}

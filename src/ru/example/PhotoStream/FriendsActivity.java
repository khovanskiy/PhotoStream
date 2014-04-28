package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import ru.example.PhotoStream.ViewAdapters.FriendListAdapter;

public class FriendsActivity extends Activity {

    private ListView friendList;
    private FriendListAdapter friendListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendsactivity);
        friendList = (ListView) findViewById(R.id.friendsactivity_friendlist);
    }

    private void update() {
        friendListAdapter = new FriendListAdapter(this);
        friendList.setAdapter(friendListAdapter);
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onFriendClick(position);
            }
        });
        for (String fid: InfoHolder.friendIds) {
            friendListAdapter.addFriend(fid);
        }
        friendListAdapter.notifyDataSetChanged();
    }

    private void onFriendClick(int position) {
        Intent intent = new Intent(this, SubstreamActivity.class);
        intent.putExtra("user", friendListAdapter.getFriendId(position));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
}
package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import ru.example.PhotoStream.ViewAdapters.GroupListAdapter;

/**
 * Created by Genyaz on 01.04.14.
 */
public class GroupsActivity extends Activity {

    private ListView groupList;
    private GroupListAdapter groupListAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groupsactivity);
        groupList = (ListView) findViewById(R.id.groupsactivity_grouplist);
    }

    private void onGroupClick(int position) {
        Intent intent = new Intent(this, SubstreamActivity.class);
        intent.putExtra("gid", groupListAdapter.getGroupId(position));
        startActivity(intent);
    }

    private void update() {
        groupListAdapter = new GroupListAdapter(this);
        groupList.setAdapter(groupListAdapter);
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onGroupClick(position);
            }
        });
        for (String groupId: InfoHolder.groupIds) {
            groupListAdapter.addGroup(groupId);
        }
        groupListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
}
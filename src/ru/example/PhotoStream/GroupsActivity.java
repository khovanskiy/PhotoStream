package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Genyaz on 01.04.14.
 */
public class GroupsActivity extends Activity {

    private class GroupListAdapter extends BaseAdapter {
        private Context context;

        private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
            ImageView bmImage;

            public DownloadImageTask(ImageView bmImage) {
                this.bmImage = bmImage;
            }

            protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
                return mIcon11;
            }

            protected void onPostExecute(Bitmap result) {
                bmImage.setImageBitmap(result);
            }
        }

        private class GroupInfo {
            String gid;
            View view;

            public GroupInfo(String gid) {
                this.gid = gid;
                JSONObject groupInfo = InfoHolder.groupInfo.get(gid);
                LinearLayout total = new LinearLayout(context);
                total.setOrientation(LinearLayout.HORIZONTAL);
                total.setGravity(Gravity.CENTER_HORIZONTAL);
                ImageView groupPhoto = new ImageView(context);
                groupPhoto.setMaxHeight(190);
                groupPhoto.setMaxWidth(190);
                try {
                    new DownloadImageTask(groupPhoto).execute(groupInfo.getString("picAvatar"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
                total.addView(groupPhoto);
                Space space = new Space(context);
                space.setMinimumWidth(10);
                total.addView(space);
                TextView groupName = new TextView(context);
                groupName.setTextColor(Color.BLACK);
                try {
                    groupName.setText(groupInfo.getString("name"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
                total.addView(groupName);
                this.view = total;
            }
        }

        private List<GroupInfo> groupInfos;

        public GroupListAdapter(Context context) {
            this.context = context;
            this.groupInfos = new ArrayList<GroupInfo>();
        }

        public void addGroup(String gid) {
            this.groupInfos.add(new GroupInfo(gid));
        }

        public String getGroupId(int position) {
            return this.groupInfos.get(position).gid;
        }

        @Override
        public int getCount() {
            return groupInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return groupInfos.get(position).view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return groupInfos.get(position).view;
        }
    }

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
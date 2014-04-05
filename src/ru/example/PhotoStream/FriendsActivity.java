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
import java.util.Date;
import java.util.List;

/**
 * Created by Genyaz on 01.04.14.
 */
public class FriendsActivity extends Activity {

    private class FriendListAdapter extends BaseAdapter {
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

        private class FriendInfo {
            String fid;
            View view;

            public FriendInfo(String fid) {
                this.fid = fid;
                JSONObject friendInfo = InfoHolder.friendInfo.get(fid);
                LinearLayout total = new LinearLayout(context);
                total.setOrientation(LinearLayout.HORIZONTAL);
                total.setGravity(Gravity.CENTER_HORIZONTAL);
                ImageView friendPhoto = new ImageView(context);
                try {
                    new DownloadImageTask(friendPhoto).execute(friendInfo.getString("pic190x190"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
                total.addView(friendPhoto);
                Space space = new Space(context);
                space.setMinimumWidth(10);
                total.addView(space);
                TextView friendName = new TextView(context);
                friendName.setTextColor(Color.BLACK);
                try {
                    friendName.setText(friendInfo.getString("name"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
                total.addView(friendName);
                this.view = total;
            }
        }

        private List<FriendInfo> friendInfos;

        public FriendListAdapter(Context context) {
            this.context = context;
            this.friendInfos = new ArrayList<FriendInfo>();
        }

        public void addFriend(String fid) {
            this.friendInfos.add(new FriendInfo(fid));
        }

        public String getFriendId(int position) {
            return this.friendInfos.get(position).fid;
        }

        @Override
        public int getCount() {
            return friendInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return friendInfos.get(position).view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return friendInfos.get(position).view;
        }
    }

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
        intent.putExtra("fid", friendListAdapter.getFriendId(position));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
}
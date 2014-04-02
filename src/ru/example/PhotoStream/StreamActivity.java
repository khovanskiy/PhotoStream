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
import org.json.JSONArray;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.InputStream;
import java.util.*;

public class StreamActivity extends Activity {
    private class PhotoListAdapter extends BaseAdapter {
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

        private class PhotoInfo {
            String photo_id;
            View view;

            public PhotoInfo(String photo_id) {
                this.photo_id = photo_id;
                ImageView photoView = new ImageView(context);
                try {
                    JSONObject photo = InfoHolder.allPhotos.get(photo_id);
                    new DownloadImageTask(photoView).execute(photo.getString("pic190x190"));
                    LinearLayout infoLayout = new LinearLayout(context);
                    infoLayout.setOrientation(LinearLayout.VERTICAL);
                    TextView owner = new TextView(context);
                    owner.setTextColor(Color.BLACK);
                    String userId = photo.getString("user_id");
                    if (InfoHolder.friendInfo.containsKey(userId)) {
                        JSONObject friend = InfoHolder.friendInfo.get(userId);
                        try {
                            owner.setText("Владелец: " + friend.getString("name"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else if (InfoHolder.groupInfo.containsKey(userId)) {
                        JSONObject group = InfoHolder.groupInfo.get(userId);
                        try {
                            owner.setText("Владелец: " + group.getString("title"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else {
                        owner.setText("Моя фотография");
                    }
                    infoLayout.addView(owner);
                    TextView album = new TextView(context);
                    album.setTextColor(Color.BLACK);
                    if (photo.has("album_id")) {
                        String albumId = photo.getString("album_id");
                        JSONObject albumObject = InfoHolder.allAlbums.get(albumId);
                        try {
                            album.setText("Альбом: " + albumObject.getString("title"));
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    } else {
                        album.setText("Личный альбом");
                    }
                    infoLayout.addView(album);
                    TextView created = new TextView(context);
                    created.setTextColor(Color.BLACK);
                    long ms = Long.parseLong(photo.getString("created_ms"));
                    Date date = new Date(ms);
                    created.setText("Загружено: " + date.toLocaleString());
                    infoLayout.addView(created);
                    LinearLayout total = new LinearLayout(context);
                    total.setOrientation(LinearLayout.HORIZONTAL);
                    total.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                    total.addView(photoView);
                    Space space = new Space(context);
                    space.setMinimumWidth(40);
                    total.addView(space);
                    total.addView(infoLayout);
                    this.view = total;
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
        }

        private List<PhotoInfo> photoInfos;

        public PhotoListAdapter(Context context) {
            this.context = context;
            this.photoInfos = new ArrayList<PhotoInfo>();
        }

        public void addPhoto(String photo_id) {
            this.photoInfos.add(new PhotoInfo(photo_id));
        }

        @Override
        public int getCount() {
            return photoInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return photoInfos.get(position).view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return photoInfos.get(position).view;
        }
    }

    private enum InfoLoadingProgress {
        GettingFriends,
        GettingGroups,
        GettingUserAlbums,
        GettingUserPhotos,
        GettingFriendAlbumsAndPhotos,
        GettingGroupAlbumsAndPhotos,
        ProcessingData,
        Done,
    }

    private Context context;

    private class InfoLoader extends AsyncTask<Void, InfoLoadingProgress, Void> {

        private List<String> getFriendIDs() {
            List<String> result = new ArrayList<String>();
            try {
                JSONArray friendIDs = new JSONArray(mOdnoklassniki.request("friends.get", null, "get"));
                for (int i = 0; i < friendIDs.length(); i++) {
                    result.add(friendIDs.getString(i));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
            return result;
        }

        private List<JSONObject> getFriendInfo(List<String> friendIds) {
            final int MAX_REQUEST = 100;
            List<JSONObject> result = new ArrayList<JSONObject>();
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("fields", "uid, locale, first_name, last_name, name, gender, age, " +
                    "birthday, has_email, location, current_location, current_status, current_status_id, " +
                    "current_status_date, online, last_online, photo_id, pic_1, pic_2, pic_3, pic_4, pic_5, " +
                    "pic50x50, pic128x128, pic128max, pic180min, pic240min, pic320min, pic190x190, pic640x480, " +
                    "pic1024x768, url_profile, url_chat, url_profile_mobile, url_chat_mobile, can_vcall, " +
                    "can_vmail, allows_anonym_access, allows_messaging_only_for_friends, registered_date, has_service_invisible");
            for (int i = 0; i < friendIds.size() / MAX_REQUEST + 1; i++) {
                StringBuilder builder = new StringBuilder();
                for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, friendIds.size()); j++) {
                    builder.append(",").append(friendIds.get(j));
                }
                requestParams.put("uids", builder.substring(1));
                try {
                    JSONArray friendInfoArray = new JSONArray(mOdnoklassniki.request("users.getInfo", requestParams, "get"));
                    for (int j = 0; j < friendInfoArray.length(); j++) {
                        result.add(friendInfoArray.getJSONObject(j));
                    }
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            return result;
        }

        private List<String> getGroupIds() {
            Map<String, String> requestParams = new HashMap<String, String>();
            List<String> result = new ArrayList<String>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject groupsObject = new JSONObject(mOdnoklassniki.request("group.getUserGroupsV2", requestParams, "get"));
                    if (groupsObject.isNull("groups")) {
                        hasMore = false;
                    } else {
                        JSONArray groups = groupsObject.getJSONArray("groups");
                        for (int i = 0; i < groups.length(); i++) {
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

        private List<JSONObject> getGroupInfo(List<String> groupIds) {
            final int MAX_REQUEST = 100;
            List<JSONObject> result = new ArrayList<JSONObject>();
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("fields", "uid, name, description, shortname, pic_avatar, photo_id, " +
                    "shop_visible_admin, shop_visible_public, members_count");
            for (int i = 0; i < groupIds.size() / MAX_REQUEST + 1; i++) {
                StringBuilder builder = new StringBuilder();
                for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, groupIds.size()); j++) {
                    builder.append(",").append(groupIds.get(j));
                }
                requestParams.put("uids", builder.substring(1));
                try {
                    JSONArray groupInfoArray = new JSONArray(mOdnoklassniki.request("group.getInfo", requestParams, "get"));
                    for (int j = 0; j < groupInfoArray.length(); j++) {
                        result.add(groupInfoArray.getJSONObject(j));
                    }
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            return result;
        }

        private List<JSONObject> getAlbums(String fid, String gid) {
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("fields", "album.*");
            if (fid != null) {
                requestParams.put("fid", fid);
            }
            if (gid != null) {
                requestParams.put("gid", gid);
            }
            List<JSONObject> result = new ArrayList<JSONObject>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject albumsObject = new JSONObject(mOdnoklassniki.request("photos.getAlbums", requestParams, "get"));
                    JSONArray albums = albumsObject.getJSONArray("albums");
                    for (int i = 0; i < albums.length(); i++) {
                        result.add(albums.getJSONObject(i));
                    }
                    hasMore = albumsObject.getBoolean("hasMore");
                    requestParams.put("anchor", albumsObject.getString("pagingAnchor"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                    hasMore = false;
                }
            }
            return result;
        }

        private List<JSONObject> getAlbumPhotos(String fid, String gid, String aid) {
            Map<String, String> requestParams = new HashMap<String, String>();
            if (fid != null) {
                requestParams.put("fid", fid);
            }
            if (gid != null) {
                requestParams.put("gid", gid);
            }
            if (aid != null) {
                requestParams.put("aid", aid);
            }
            requestParams.put("fields", "photo.*");
            List<JSONObject> result = new ArrayList<JSONObject>();
            boolean hasMore = true;
            while (hasMore) {
                try {
                    JSONObject photosObject = new JSONObject(mOdnoklassniki.request("photos.getPhotos", requestParams, "get"));
                    JSONArray photos = photosObject.getJSONArray("photos");
                    for (int i = 0; i < photos.length(); i++) {
                        result.add(photos.getJSONObject(i));
                    }
                    hasMore = photosObject.getBoolean("hasMore");
                    if (hasMore) {
                        requestParams.put("anchor", photosObject.getString("anchor"));
                    }
                } catch (Exception e) {
                    Console.print(e.getMessage());
                    hasMore = false;
                }
            }
            Collections.sort(result, new InfoHolder.PhotoByUploadTimeComparator());
            return result;
        }

        @Override
        protected Void doInBackground(Void... params) {
            InfoHolder.clear();
            //Getting friends
            publishProgress(InfoLoadingProgress.GettingFriends);
            InfoHolder.friendIds = getFriendIDs();
            List<JSONObject> friendInfo = getFriendInfo(InfoHolder.friendIds);
            for (int i = 0; i < friendInfo.size(); i++) {
                JSONObject friend = friendInfo.get(i);
                try {
                    InfoHolder.friendInfo.put(friend.getString("uid"), friend);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            //Getting groups
            publishProgress(InfoLoadingProgress.GettingGroups);
            InfoHolder.groupIds = getGroupIds();
            List<JSONObject> groupInfo = getGroupInfo(InfoHolder.groupIds);
            for (int i = 0; i < groupInfo.size(); i++) {
                JSONObject group = groupInfo.get(i);
                try {
                    InfoHolder.groupInfo.put(group.getString("uid"), group);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            //Getting user albums
            publishProgress(InfoLoadingProgress.GettingUserAlbums);
            InfoHolder.userAlbums = getAlbums(null, null);
            //Getting user photos
            publishProgress(InfoLoadingProgress.GettingUserPhotos);
            for (int i = 0; i < InfoHolder.userAlbums.size(); i++) {
                try {
                    JSONObject album = InfoHolder.userAlbums.get(i);
                    String aid = album.getString("aid");
                    SortedSet<JSONObject> albumPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
                    albumPhotos.addAll(getAlbumPhotos(null, null, aid));
                    InfoHolder.albumPhotos.put(aid, albumPhotos);
                    InfoHolder.allAlbums.put(aid, album);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            InfoHolder.userPrivatePhotos.addAll(getAlbumPhotos(null, null, null));
            //Getting friend albums and photos
            publishProgress(InfoLoadingProgress.GettingFriendAlbumsAndPhotos);
            for (int i = 0; i < InfoHolder.friendIds.size(); i++) {
                List<JSONObject> friendAlbums = getAlbums(InfoHolder.friendIds.get(i), null);
                for (int j = 0; j < friendAlbums.size(); j++) {
                    try {
                        JSONObject album = friendAlbums.get(j);
                        String aid = album.getString("aid");
                        SortedSet<JSONObject> albumPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
                        albumPhotos.addAll(getAlbumPhotos(InfoHolder.friendIds.get(i), null, aid));
                        InfoHolder.albumPhotos.put(aid, albumPhotos);
                        InfoHolder.allAlbums.put(aid, album);
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                }
                InfoHolder.friendAlbums.put(InfoHolder.friendIds.get(i), friendAlbums);
                SortedSet<JSONObject> privatePhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
                privatePhotos.addAll(getAlbumPhotos(InfoHolder.friendIds.get(i), null, null));
                InfoHolder.friendPrivatePhotos.put(InfoHolder.friendIds.get(i), privatePhotos);
            }
            //Getting group albums and photos
            publishProgress(InfoLoadingProgress.GettingGroupAlbumsAndPhotos);
            for (int i = 0; i < InfoHolder.groupIds.size(); i++) {
                try {
                    List<JSONObject> groupAlbums = getAlbums(null, InfoHolder.groupIds.get(i));
                    for (int j = 0; j < groupAlbums.size(); j++) {
                        try {
                            JSONObject album = groupAlbums.get(j);
                            String aid = album.getString("aid");
                            SortedSet<JSONObject> albumPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
                            albumPhotos.addAll(getAlbumPhotos(null, InfoHolder.groupIds.get(i), aid));
                            InfoHolder.albumPhotos.put(aid, albumPhotos);
                            InfoHolder.allAlbums.put(aid, album);
                        } catch (Exception e) {
                            Console.print(e.getMessage());
                        }
                    }
                    InfoHolder.groupAlbums.put(InfoHolder.groupIds.get(i), groupAlbums);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            //Processing data
            publishProgress(InfoLoadingProgress.ProcessingData);
            for (JSONObject photo: InfoHolder.userPrivatePhotos) {
                try {
                    InfoHolder.allPhotos.put(photo.getString("id"), photo);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            InfoHolder.sortedPhotos.addAll(InfoHolder.userPrivatePhotos);
            for (SortedSet<JSONObject> photos : InfoHolder.friendPrivatePhotos.values()) {
                for (JSONObject photo: photos) {
                    try {
                        InfoHolder.allPhotos.put(photo.getString("id"), photo);
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                }
                InfoHolder.sortedPhotos.addAll(photos);
            }
            for (SortedSet<JSONObject> photos : InfoHolder.albumPhotos.values()) {
                for (JSONObject photo: photos) {
                    try {
                        InfoHolder.allPhotos.put(photo.getString("id"), photo);
                    } catch (Exception e) {
                        Console.print(e.getMessage());
                    }
                }
                InfoHolder.sortedPhotos.addAll(photos);
            }
            publishProgress(InfoLoadingProgress.Done);
            return null;
        }

        @Override
        protected void onProgressUpdate(InfoLoadingProgress... values) {
            for (int i = 0; i < values.length; i++) {
                String waitingText = "";
                switch (values[i]) {
                    case GettingFriends:
                        waitingText = "Получение списка друзей";
                        break;
                    case GettingGroups:
                        waitingText = "Получение списка групп";
                        break;
                    case GettingUserAlbums:
                        waitingText = "Получение ваших альбомов";
                        break;
                    case GettingUserPhotos:
                        waitingText = "Получение ваших фотографий";
                        break;
                    case GettingFriendAlbumsAndPhotos:
                        waitingText = "Получение альбомов и фотографий друзей";
                        break;
                    case GettingGroupAlbumsAndPhotos:
                        waitingText = "Получение альбомов и фотографий групп";
                        break;
                    case ProcessingData:
                        waitingText = "Обработка данных";
                        break;
                    case Done:
                        waitingText = "Загрузка завершена";
                        break;
                }
                ((TextView) findViewById(R.id.please_stand_by_text)).setText(waitingText);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            setContentView(R.layout.streamactivity);
            photoList = (ListView) findViewById(R.id.streamactivity_photolist);
            photoListAdapter = new PhotoListAdapter(context);
            photoList.setAdapter(photoListAdapter);
            new ImageLoader().execute(null);
        }
    }

    private class ImageLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            for (JSONObject photo: InfoHolder.sortedPhotos) {
                try {
                    photoListAdapter.addPhoto(photo.getString("id"));
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            photoListAdapter.notifyDataSetChanged();
        }
    }

    private ListView photoList;
    private PhotoListAdapter photoListAdapter;
    private Odnoklassniki mOdnoklassniki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.please_stand_by);
        context = this;
        mOdnoklassniki = Odnoklassniki.getInstance(getApplicationContext());
        update();
    }

    private void update() {
        new InfoLoader().execute();
    }

    public void onMyAlbumsClick(View view) {
        Intent intent = new Intent(this, UserAlbumsActivity.class);
        startActivity(intent);
    }

    public void onFriendAlbumsClick(View view) {
        Intent intent = new Intent(this, FriendAlbumsActivity.class);
        startActivity(intent);
    }

    public void onGroupAlbumsClick(View view) {
        Intent intent = new Intent(this, GroupAlbumsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

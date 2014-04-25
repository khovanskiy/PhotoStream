package ru.example.PhotoStream;

import android.os.AsyncTask;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.*;

class InfoLoader extends AsyncTask<Void, InfoLoadingProgress, Void> implements IEventDispatcher{

    private Odnoklassniki mOdnoklassniki;
    private EventDispatcher eventDispatcher;

    public InfoLoader(Odnoklassniki mOdnoklassniki)
    {
            this.mOdnoklassniki = mOdnoklassniki;
    }

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
                InfoHolder.userPhotos.addAll(albumPhotos);
                InfoHolder.allAlbums.put(aid, album);
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        InfoHolder.userPrivatePhotos.addAll(getAlbumPhotos(null, null, null));
        InfoHolder.userPhotos.addAll(InfoHolder.userPrivatePhotos);
        //Getting friend albums and photos
        publishProgress(InfoLoadingProgress.GettingFriendAlbumsAndPhotos);
        for (int i = 0; i < InfoHolder.friendIds.size(); i++) {
            SortedSet<JSONObject> friendPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
            String fid = InfoHolder.friendIds.get(i);
            List<JSONObject> friendAlbums = getAlbums(fid, null);
            for (int j = 0; j < friendAlbums.size(); j++) {
                try {
                    JSONObject album = friendAlbums.get(j);
                    String aid = album.getString("aid");
                    SortedSet<JSONObject> albumPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
                    albumPhotos.addAll(getAlbumPhotos(fid, null, aid));
                    friendPhotos.addAll(albumPhotos);
                    InfoHolder.albumPhotos.put(aid, albumPhotos);
                    InfoHolder.allAlbums.put(aid, album);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            InfoHolder.friendAlbums.put(fid, friendAlbums);
            SortedSet<JSONObject> privatePhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
            privatePhotos.addAll(getAlbumPhotos(fid, null, null));
            InfoHolder.friendPrivatePhotos.put(fid, privatePhotos);
            friendPhotos.addAll(privatePhotos);
            InfoHolder.friendPhotos.put(fid, friendPhotos);
        }
        //Getting group albums and photos
        publishProgress(InfoLoadingProgress.GettingGroupAlbumsAndPhotos);
        for (int i = 0; i < InfoHolder.groupIds.size(); i++) {
            String gid = InfoHolder.groupIds.get(i);
            SortedSet<JSONObject> groupPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
            List<JSONObject> groupAlbums = getAlbums(null, gid);
            for (int j = 0; j < groupAlbums.size(); j++) {
                try {
                    JSONObject album = groupAlbums.get(j);
                    String aid = album.getString("aid");
                    SortedSet<JSONObject> albumPhotos = new TreeSet<JSONObject>(new InfoHolder.PhotoByUploadTimeComparator());
                    albumPhotos.addAll(getAlbumPhotos(null, gid, aid));
                    groupPhotos.addAll(albumPhotos);
                    InfoHolder.albumPhotos.put(aid, albumPhotos);
                    InfoHolder.allAlbums.put(aid, album);
                } catch (Exception e) {
                    Console.print(e.getMessage());
                }
            }
            InfoHolder.groupAlbums.put(InfoHolder.groupIds.get(i), groupAlbums);
            InfoHolder.groupPhotos.put(gid, groupPhotos);
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
        InfoHolder.infoDownloaded = true;
        publishProgress(InfoLoadingProgress.Done);
        return null;
    }

    @Override
    protected void onProgressUpdate(InfoLoadingProgress... values) {
        /*for (int i = 0; i < values.length; i++) {
            String waitingText = "";
            switch (values[i]) {
                case GettingFriends:
                    waitingText = getString(R.string.getting_friend_list);
                    break;
                case GettingGroups:
                    waitingText = getString(R.string.getting_group_list);
                    break;
                case GettingUserAlbums:
                    waitingText = getString(R.string.getting_your_albums);
                    break;
                case GettingUserPhotos:
                    waitingText = getString(R.string.getting_your_photos);
                    break;
                case GettingFriendAlbumsAndPhotos:
                    waitingText = getString(R.string.getting_friends_albums_and_photos);
                    break;
                case GettingGroupAlbumsAndPhotos:
                    waitingText = getString(R.string.getting_groups_albums_and_photos);
                    break;
                case ProcessingData:
                    waitingText = getString(R.string.processing_data);
                    break;
                case Done:
                    waitingText = getString(R.string.download_is_finished);
                    break;
            }
            ((TextView) findViewById(R.id.please_stand_by_text)).setText(waitingText);
        }  */
    }

    @Override
    protected void onPostExecute(Void result) {
        dispatchEvent(new Event(this, Event.COMPLETE));
    }

    @Override
    public void addEventListener(IEventHadler listener) {
        eventDispatcher.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventHadler listener) {
        eventDispatcher.removeEventListener(listener);
    }

    @Override
    public void dispatchEvent(Event e) {
        eventDispatcher.dispatchEvent(e);
    }
}
package ru.example.PhotoStream;


import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataLoader extends AsyncTask<Void, Void, List<?>> implements IEventDispatcher {

    private EventDispatcher eventDispatcher;
    protected Odnoklassniki api;

    public DataLoader(Odnoklassniki api) {
        this.api = api;
        eventDispatcher = new EventDispatcher();
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

    protected List<Group> getGroupInfo(List<String> groupIds) {
        final int MAX_REQUEST = 100;
        List<Group> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "uid, name, description, shortname, pic_avatar, photo_id, " +
                "shop_visible_admin, shop_visible_public, members_count");
        for (int i = 0; i < groupIds.size() / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, groupIds.size()); ++j) {
                builder.append(",").append(groupIds.get(j));
            }
            if (builder.length() == 0) {
                break;
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray groupInfoArray = new JSONArray(api.request("group.getInfo", requestParams, "get"));
                for (int j = 0; j < groupInfoArray.length(); ++j) {
                    result.add(Group.build(groupInfoArray.getJSONObject(j)));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        return result;
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

    protected List<User> getUserInfo(List<String> usersIds) {
        final int MAX_REQUEST = 100;
        List<User> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "uid, locale, first_name, last_name, name, gender, age, " +
                "birthday, has_email, location, current_location, current_status, current_status_id, " +
                "current_status_date, online, last_online, photo_id, pic_1, pic_2, pic_3, pic_4, pic_5, " +
                "pic50x50, pic128x128, pic128max, pic180min, pic240min, pic320min, pic190x190, pic640x480, " +
                "pic1024x768, url_profile, url_chat, url_profile_mobile, url_chat_mobile, can_vcall, " +
                "can_vmail, allows_anonym_access, allows_messaging_only_for_friends, registered_date, has_service_invisible");
        for (int i = 0; i < usersIds.size() / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, usersIds.size()); ++j) {
                builder.append(",").append(usersIds.get(j));
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray friendInfoArray = new JSONArray(api.request("users.getInfo", requestParams, "get"));
                for (int j = 0; j < friendInfoArray.length(); ++j) {
                    result.add(User.build(friendInfoArray.getJSONObject(j)));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        return result;
    }

    protected Photo getPhoto(String photo_id, String gid) {
        Map<String, String> requestParams = new HashMap<String, String>();
        if (photo_id != null) {
            requestParams.put("photo_id", photo_id);
        }
        if (gid != null) {
            requestParams.put("gid", gid);
            requestParams.put("fields", "group_photo.*");
        }
        Photo result = null;
        try {
            String response = api.request("photos.getPhotoInfo", requestParams, "get");
            JSONObject photosObject = new JSONObject(response);
            result = Photo.build(photosObject.getJSONObject("photo"));
        } catch (Exception e) {
            Console.print("Error " + e.getMessage());
        }
        return result;
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

package ru.example.PhotoStream;


import android.os.AsyncTask;
import org.json.JSONArray;
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

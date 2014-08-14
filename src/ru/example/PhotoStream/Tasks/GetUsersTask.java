package ru.example.PhotoStream.Tasks;

import android.util.Log;
import org.json.JSONArray;
import ru.example.PhotoStream.User;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetUsersTask implements Task<List<User>> {

    private final List<String> uids;

    public GetUsersTask(List<String> uids) {
        this.uids = uids;
    }

    @Override
    public List<User> execute(Odnoklassniki api) {
        final int MAX_REQUEST = 100;
        List<User> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "uid, locale, first_name, last_name, name, gender, age, " +
                "birthday, has_email, location, current_location, current_status, current_status_id, " +
                "current_status_date, online, last_online, photo_id, pic_1, pic_2, pic_3, pic_4, pic_5, " +
                "pic50x50, pic128x128, pic128max, pic180min, pic240min, pic320min, pic190x190, pic640x480, " +
                "pic1024x768, url_profile, url_chat, url_profile_mobile, url_chat_mobile, can_vcall, " +
                "can_vmail, allows_anonym_access, allows_messaging_only_for_friends, registered_date, has_service_invisible");
        for (int i = 0; i < uids.size() / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, uids.size()); ++j) {
                builder.append(",").append(uids.get(j));
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray friendInfoArray = new JSONArray(api.request("users.getInfo", requestParams, "get"));
                for (int j = 0; j < friendInfoArray.length(); ++j) {
                    result.add(User.build(friendInfoArray.getJSONObject(j)));
                }
            } catch (Exception e) {
                Log.d("TASK_ERROR", e.getMessage(), e);
            }
        }
        return result;
    }
}

package ru.example.PhotoStream.Tasks;

import android.util.Log;
import org.json.JSONArray;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.Photo;
import ru.example.PhotoStream.User;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetUsersTask implements Callable<List<User>> {

    private final Odnoklassniki api;
    private final String[] uids;

    public GetUsersTask(Odnoklassniki api, String... uids) {
        this.api = api;
        this.uids = uids;
    }

    @Override
    public List<User> call() {
        final int MAX_REQUEST = 100;
        List<User> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "uid, first_name, last_name, name, photo_id");
        for (int i = 0; i < uids.length / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, uids.length); ++j) {
                builder.append(",").append(uids[j]);
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray friendInfoArray = new JSONArray(api.request("users.getInfo", requestParams, "get"));
                for (int j = 0; j < friendInfoArray.length(); ++j) {
                    User user = User.build(friendInfoArray.getJSONObject(j));
                    Callable<Photo> callable = new GetPhotoTask(api, user.getAvatar().id, user);
                    callable.call();
                    result.add(user);
                }
            } catch (Exception e) {
                Log.d("TASK_ERROR", e.getMessage(), e);
            }
        }
        return result;
    }
}

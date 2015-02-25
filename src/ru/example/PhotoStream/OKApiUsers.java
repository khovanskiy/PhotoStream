package ru.example.PhotoStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.example.PhotoStream.Activities.UIActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OKApiUsers extends OKApiBase {

    private static final int MAX_USERS_PER_REQUEST = 100;

    public OKRequest get() {
        return prepareRequest("users.getCurrentUser", null, new OKParser() {
            @Override
            public Object createModel(JSONObject object) throws JSONException {
                return User.build(object);
            }
        });
    }

    public OKRequest getById(final OKParameters params) {
        return new OKRequest("users.getInfo", params) {

            @Override
            protected void start() {
                String[] uids = params.get("uids").split(",");
                provideResponse(usersInfo(uids, params));
            }
        };
    }

    public static List<User> usersInfo(String[] uids, OKParameters params) {
        List<User> users = new ArrayList<>(uids.length);
        for (int i = 0; i < uids.length / MAX_USERS_PER_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_USERS_PER_REQUEST; j < Math.min((i + 1) * MAX_USERS_PER_REQUEST, uids.length); ++j) {
                builder.append(",").append(uids[j]);
            }
            OKParameters requestParams = new OKParameters();
            requestParams.put("uids", builder.substring(1));
            requestParams.put("fields", "uid, first_name, last_name, name, photo_id");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!entry.getKey().equals("uids")) {
                    requestParams.put(entry.getKey(), entry.getValue());
                }
            }
            try {
                JSONArray array = new JSONArray(UIActivity.getAPI().request("users.getInfo", requestParams, "get"));
                for (int j = 0; j < array.length(); ++j) {
                    User user = User.build(array.getJSONObject(j));
                    users.add(user);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return users;
    }
}

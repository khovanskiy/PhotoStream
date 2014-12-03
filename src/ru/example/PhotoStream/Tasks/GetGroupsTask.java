package ru.example.PhotoStream.Tasks;

import org.json.JSONArray;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.Group;
import ru.example.PhotoStream.Photo;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetGroupsTask implements Callable<List<Group>> {

    private final Odnoklassniki api;

    private final String[] uids;

    public GetGroupsTask(Odnoklassniki api, String... uids) {
        this.api = api;
        this.uids = uids;
    }

    @Override
    public List<Group> call() throws Exception {
        final int MAX_REQUEST = 100;
        List<Group> result = new ArrayList<>();
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("fields", "group.*");
        for (int i = 0; i < uids.length / MAX_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_REQUEST; j < Math.min((i + 1) * MAX_REQUEST, uids.length); ++j) {
                builder.append(",").append(uids[j]);
            }
            if (builder.length() == 0) {
                break;
            }
            requestParams.put("uids", builder.substring(1));
            try {
                JSONArray groupInfoArray = new JSONArray(api.request("group.getInfo", requestParams, "get"));
                for (int j = 0; j < groupInfoArray.length(); ++j) {
                    Group group = Group.build(groupInfoArray.getJSONObject(j));
                    Callable<Photo> callable = new GetPhotoTask(api, group.getAvatar().id, group);
                    callable.call();
                    result.add(group);
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        return result;
    }
}

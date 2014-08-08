package ru.example.PhotoStream.Loaders;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsLoader extends TLoader<List<Group>> {

    protected Odnoklassniki api;
    public GroupsLoader(Odnoklassniki api) {
        this.api = api;
    }

    @Override
    protected List<Group> doInBackground(Void... params) {
        List<String> gids = getGroupIds();
        List<Group> groups = getGroupInfo(gids);
        /*for (Group group : groups) {
            Photo photo = getPhoto(group.photo_id, group.uid);
            if (photo != null) {
                //group.photo = photo;
            }
        }*/
        return groups;
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
}

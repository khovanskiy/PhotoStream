package ru.example.PhotoStream;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.example.PhotoStream.Activities.UIActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OKApiGroups extends OKApiBase {

    private static final int MAX_USERS_PER_REQUEST = 100;

    public OKRequest get(final OKParameters params) {
        return new OKRequest("group.getUserGroupsV2", params) {
            @Override
            protected void start() {
                boolean hasMore = true;
                OKParameters requestParams = new OKParameters();
                List<String> groupsIds = new ArrayList<>();
                while (hasMore) {
                    try {
                        JSONObject groupsObject = new JSONObject(UIActivity.getAPI().request(methodName, requestParams, "get"));
                        if (groupsObject.isNull("groups")) {
                            hasMore = false;
                        } else {
                            JSONArray groups = groupsObject.getJSONArray("groups");
                            for (int i = 0; i < groups.length(); ++i) {
                                groupsIds.add(groups.getJSONObject(i).getString("groupId"));
                            }
                            requestParams.put("anchor", groupsObject.getString("anchor"));
                        }
                    } catch (Exception e) {
                        hasMore = false;
                    }
                }
                if (params.containsKey("fields")) {
                    provideResponse(groupsInfo(groupsIds.toArray(new String[groupsIds.size()]), params));
                } else {
                    provideResponse(groupsIds);
                }
            }
        };
    }

    public OKRequest getById(final OKParameters params) {
        return new OKRequest("group.getInfo", params) {
            @Override
            protected void start() {
                String[] uids = params.get("uids").split(",");
                provideResponse(groupsInfo(uids, params));
            }
        };
    }

    public static List<Group> groupsInfo(final String[] uids, final OKParameters params) {
        List<Group> groups = new ArrayList<>(uids.length);
        for (int i = 0; i < uids.length / MAX_USERS_PER_REQUEST + 1; ++i) {
            StringBuilder builder = new StringBuilder();
            for (int j = i * MAX_USERS_PER_REQUEST; j < Math.min((i + 1) * MAX_USERS_PER_REQUEST, uids.length); ++j) {
                builder.append(",").append(uids[j]);
            }
            OKParameters requestParams = new OKParameters();
            requestParams.put("uids", builder.substring(1));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!entry.getKey().equals("uids")) {
                    requestParams.put(entry.getKey(), entry.getValue());
                }
            }
            try {
                JSONArray array = new JSONArray(UIActivity.getAPI().request("group.getInfo", requestParams, "get"));
                for (int j = 0; j < array.length(); ++j) {
                    Group group = Group.build(array.getJSONObject(j));
                    groups.add(group);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return groups;
    }
}

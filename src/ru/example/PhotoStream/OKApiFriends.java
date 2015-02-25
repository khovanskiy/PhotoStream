package ru.example.PhotoStream;

import org.json.JSONArray;
import ru.example.PhotoStream.Activities.UIActivity;

import java.util.ArrayList;
import java.util.List;

public class OKApiFriends extends OKApiBase {
    public OKRequest get(final OKParameters params) {
        return new OKRequest("friends.get", params) {
            @Override
            protected void start() {
                List<String> usersIds = new ArrayList<>();
                try {
                    JSONArray friendIDs = new JSONArray(UIActivity.getAPI().request(methodName, params, "get"));
                    for (int i = 0; i < friendIDs.length(); ++i) {
                        usersIds.add(friendIDs.getString(i));
                    }
                } catch (Exception e) {
                    provideError(new OKError(e.getMessage()));
                }
                if (params.containsKey("fields")) {
                    provideResponse(OKApiUsers.usersInfo(usersIds.toArray(new String[usersIds.size()]), params));
                } else {
                    provideResponse(usersIds);
                }
            }
        };
    }
}

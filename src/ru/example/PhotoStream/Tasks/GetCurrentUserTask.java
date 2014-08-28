package ru.example.PhotoStream.Tasks;

import org.json.JSONObject;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.User;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetCurrentUserTask implements Callable<User> {

    protected Odnoklassniki api;

    public GetCurrentUserTask(Odnoklassniki api) {
        this.api = api;
    }

    @Override
    public User call() throws Exception {
        Map<String, String> requestParams = new HashMap<>();
        String response = api.request("users.getCurrentUser", requestParams, "get");
        JSONObject userObject = new JSONObject(response);
        String uid = userObject.getString("uid");
        Callable<List<User>> callable = new GetUsersTask(api, uid);
        return callable.call().get(0);
    }
}

package ru.example.PhotoStream.Loaders;

import ru.example.PhotoStream.DataLoader;
import ru.example.PhotoStream.Event;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public class FriendsLoader extends DataLoader
{
    public FriendsLoader(Odnoklassniki api) {
        super(api);
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        return getUserInfo(getFriendIDs());
    }

    @Override
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.FRIENDS_LOADED);
        e.data.put("friends", data);
        dispatchEvent(e);
    }
}

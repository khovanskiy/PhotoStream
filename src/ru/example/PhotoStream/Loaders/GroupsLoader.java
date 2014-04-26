package ru.example.PhotoStream.Loaders;

import ru.example.PhotoStream.DataLoader;
import ru.example.PhotoStream.Event;
import ru.example.PhotoStream.Group;
import ru.example.PhotoStream.Photo;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public class GroupsLoader extends DataLoader
{
    public GroupsLoader(Odnoklassniki api) {
        super(api);
    }

    @Override
    protected List<?> doInBackground(Void... params) {
        List<String> gids = getGroupIds();
        List<Group> groups =  getGroupInfo(gids);
        for (Group group : groups)
        {
            Photo photo = getPhoto(group.photo_id, group.uid);
            group.photo = photo;
        }
        return groups;
    }

    @Override
    protected void onPostExecute(List<?> data) {
        Event e = new Event(this, Event.COMPLETE);
        e.data.put("groups", data);
        dispatchEvent(e);
    }
}

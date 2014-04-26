package ru.example.PhotoStream;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import ru.example.PhotoStream.Loaders.FriendsLoader;
import ru.example.PhotoStream.Loaders.PhotosLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment implements IEventHadler{

    static class UsersAdapter extends BaseAdapter {

        private List<User> users = new ArrayList<>();
        private Context context;

        public UsersAdapter(Context context)
        {
            this.context = context;
        }

        public void addUser(User user)
        {
            users.add(user);
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Console.print("get view " + position + " " + users.size());
            User user = users.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.friendsbadgeview, parent, false);
            SmartImage imageView = (SmartImage) view.findViewById(R.id.friendsbadgeview_image);
            imageView.loadFromURL(user.pic190x190);
            TextView title = (TextView) view.findViewById(R.id.friendsbadgeview_title);
            title.setText(user.first_name + " " + user.last_name);
            return view;
        }
    }

    private Odnoklassniki api;
    private UsersAdapter photoListAdapter;
    private GridView photoList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = Odnoklassniki.getInstance(getActivity());

        DataLoader loader = new FriendsLoader(api);
        loader.addEventListener(this);
        loader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friendsactivity, container, false);
        photoList = (GridView) view.findViewById(R.id.friendsactivity_friendlist);
        return view;
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE)
        {
            List<User> users = (List<User>) e.data.get("friends");
            Console.print("Complete loader");
            photoListAdapter = new UsersAdapter(getActivity());
            photoList.setAdapter(photoListAdapter);

            for (User user : users)
            {
                Console.print(user.uid + " " + user.first_name);
                photoListAdapter.addUser(user);
            }

            photoListAdapter.notifyDataSetChanged();
        }
    }
}

package ru.example.PhotoStream.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import ru.example.PhotoStream.Activities.AlbumsActivity;
import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment implements AdapterView.OnItemClickListener {

    static class UsersAdapter extends BaseAdapter {

        private List<User> users = new ArrayList<>();
        private Context context;

        public UsersAdapter(Context context) {
            this.context = context;
        }

        public void addUser(User user) {
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
    private GridView usersList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = Odnoklassniki.getInstance(getActivity());

        UsersAdapter usersAdapter = new UsersAdapter(getActivity());
        usersList.setAdapter(usersAdapter);

        List<User> users = User.getAllUsers();
        for (User user : users) {
            if (!user.uid.equals("")) {
                usersAdapter.addUser(user);
            }
        }

        usersAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friendsactivity, container, false);
        usersList = (GridView) view.findViewById(R.id.friendsactivity_friendlist);
        usersList.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), AlbumsActivity.class);
        User obj = (User) usersList.getItemAtPosition(position);
        intent.putExtra("uid", obj.uid);
        startActivity(intent);
    }
}

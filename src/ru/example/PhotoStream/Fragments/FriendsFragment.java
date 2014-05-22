package ru.example.PhotoStream.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import ru.example.PhotoStream.Activities.AlbumsActivity;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.ViewAdapters.PhotosAdapter;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends IFragmentSwitcher implements AdapterView.OnItemClickListener {

    private class UsersAdapter extends BaseAdapter {

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

        public void clear() {
            users.clear();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            User user = users.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.friendsbadgeview, parent, false);
            GridView photosList = (GridView) view.findViewById(R.id.friendsbadgeview_grid);
            PhotosAdapter photosAdapter = (PhotosAdapter) photosList.getAdapter();
            if (photosAdapter == null) {
                photosAdapter = new PhotosAdapter(context, true);
                photosList.setAdapter(photosAdapter);
            }
            photosList.setNumColumns(PREVIEWS_PER_BADGE);
            photosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                    FriendsFragment.this.onItemClick(null, null, position, id);
                }
            });

            TextView title = (TextView) view.findViewById(R.id.friendsbadgeview_title);
            title.setText(user.name);

            List<Album> albums = user.getAlbums();
            int count = 0;
            photosAdapter.clear();
            loop:
            for (Album album : albums) {
                for (int j = 0; j < album.chunksCount(); ++j) {
                    List<Photo> photos = album.getChunk(j);
                    for (int k = 0; k < photos.size(); ++k) {
                        ++count;
                        photosAdapter.addPhoto(photos.get(k));
                        if (count == PREVIEWS_PER_BADGE) {
                            break loop;
                        }
                    }
                }
            }
            photosAdapter.notifyDataSetChanged();
            return view;
        }
    }

    private GridView usersList;
    protected final static int PREVIEWS_PER_BADGE = 3;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

    @Override
    public void onVisible() {
        super.onVisible();
        UsersAdapter usersAdapter = (UsersAdapter) usersList.getAdapter();
        if (usersAdapter == null) {
            usersAdapter = new UsersAdapter(getActivity());
            usersList.setAdapter(usersAdapter);
        }
        usersAdapter.clear();
        List<User> groups = User.getAllUsers();
        for (User user : groups) {
            if (!user.uid.equals("")) {
                usersAdapter.addUser(user);
            }
        }
        usersAdapter.notifyDataSetChanged();
    }
}

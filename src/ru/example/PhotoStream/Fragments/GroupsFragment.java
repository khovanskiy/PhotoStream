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
import ru.example.PhotoStream.Loaders.GroupsLoader;
import ru.example.PhotoStream.ViewAdapters.PhotosAdapter;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment implements AdapterView.OnItemClickListener {

    static class GroupsAdapter extends BaseAdapter {

        private List<Group> groups = new ArrayList<>();
        private Context context;

        public GroupsAdapter(Context context) {
            this.context = context;
        }

        public void addGroup(Group group) {
            groups.add(group);
        }

        @Override
        public int getCount() {
            return groups.size();
        }

        @Override
        public Object getItem(int position) {
            return groups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = groups.get(position);
            Console.print("==Group: " + group.name + " ==");

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.groupbadgeview, parent, false);
            GridView photosList = (GridView) view.findViewById(R.id.groupbadgeview_grid);
            PhotosAdapter groupsAdapter = (PhotosAdapter) photosList.getAdapter();
            if (groupsAdapter == null) {
                groupsAdapter = new PhotosAdapter(context);
                photosList.setAdapter(groupsAdapter);
            }
            TextView title = (TextView) view.findViewById(R.id.groupbadgeview_title);
            title.setText(group.name);
            if (group.getAlbums().size() > 0) {
                Console.print("Has albums " + group.getAlbums().size());
                Album album = group.getAlbums().get(0);
                if (album.chunksCount() > 0) {
                    Console.print("Has first chunk");
                    List<Photo> photos = album.getChunk(0);
                    if (photos.size() > 0) {
                        Console.print("Has photos there " + photos.size());
                        groupsAdapter.clear();
                        for (int i = 0; i < photos.size(); ++i) {
                            Console.print("Photo: " + photos.get(i).pic180min);
                            groupsAdapter.addPhoto(photos.get(i));
                        }
                        groupsAdapter.notifyDataSetChanged();
                    }
                }
            }
            return view;
        }
    }

    private Odnoklassniki api;
    private GridView groupsList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = Odnoklassniki.getInstance(getActivity());

        GroupsAdapter groupsAdapter = new GroupsAdapter(getActivity());
        groupsList.setAdapter(groupsAdapter);

        List<Group> groups = Group.getAllGroups();
        for (Group group : groups) {
            groupsAdapter.addGroup(group);
        }

        groupsAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groupsactivity, container, false);
        groupsList = (GridView) view.findViewById(R.id.groupsactivity_grouplist);
        groupsList.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), AlbumsActivity.class);
        Group obj = (Group) groupsList.getItemAtPosition(position);
        intent.putExtra("gid", obj.uid);
        startActivity(intent);
    }
}

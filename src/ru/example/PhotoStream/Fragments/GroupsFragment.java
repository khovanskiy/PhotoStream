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


public class GroupsFragment extends IFragmentSwitcher implements AdapterView.OnItemClickListener, View.OnLayoutChangeListener {

    private class GroupsAdapter extends BaseAdapter {

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

        public void clear() {
            groups.clear();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Group group = groups.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.groupbadgeview, parent, false);
            GridView photosList = (GridView) view.findViewById(R.id.groupbadgeview_grid);
            PhotosAdapter photosAdapter = (PhotosAdapter) photosList.getAdapter();
            if (photosAdapter == null) {
                photosAdapter = new PhotosAdapter(context, true);
                photosList.setAdapter(photosAdapter);
            }
            photosList.setNumColumns(PREVIEWS_PER_BADGE);
            photosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                    GroupsFragment.this.onItemClick(null, null, position, id);
                }
            });

            TextView title = (TextView) view.findViewById(R.id.groupbadgeview_title);
            title.setText(group.name);

            List<Album> albums = group.getAlbums();
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

    private GridView groupsList;
    protected final static int PREVIEWS_PER_BADGE = 3;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groupsactivity, container, false);
        groupsList = (GridView) view.findViewById(R.id.groupsactivity_grouplist);
        groupsList.setOnItemClickListener(this);
        groupsList.addOnLayoutChangeListener(this);
        return view;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;
        int currentWidth = right - left;
        int currentHeight = bottom - top;
        if (oldWidth != currentWidth || oldHeight != currentHeight) {
            int columns = (int) Math.ceil(currentWidth / (PREVIEWS_PER_BADGE * 180.0));
            groupsList.setNumColumns(columns);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), AlbumsActivity.class);
        Group obj = (Group) groupsList.getItemAtPosition(position);
        intent.putExtra("gid", obj.getId());
        startActivity(intent);
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        GroupsAdapter groupsAdapter = (GroupsAdapter) groupsList.getAdapter();
        if (groupsAdapter == null) {
            groupsAdapter = new GroupsAdapter(getActivity());
            groupsList.setAdapter(groupsAdapter);
        }
        groupsAdapter.clear();
        List<Group> groups = Group.getAllGroups();
        for (Group group : groups) {
            groupsAdapter.addGroup(group);
        }
        groupsAdapter.notifyDataSetChanged();
    }
}

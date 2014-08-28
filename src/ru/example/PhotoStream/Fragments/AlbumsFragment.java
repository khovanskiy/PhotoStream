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
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Activities.AlbumActivity;
import ru.example.PhotoStream.ViewAdapters.PhotosAdapter;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends IFragmentSwitcher implements AdapterView.OnItemClickListener, View.OnLayoutChangeListener {

    private class AlbumsAdapter extends BaseAdapter {

        private List<Album> albums = new ArrayList<>();
        private Context context;

        public AlbumsAdapter(Context context) {
            this.context = context;
        }

        public void add(Album album) {
            albums.add(album);
        }

        @Override
        public int getCount() {
            return albums.size();
        }

        @Override
        public Object getItem(int position) {
            return albums.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void clear() {
            albums.clear();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Album album = albums.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.badgeview, parent, false);
            assert(false);
            GridView photosList = (GridView) view.findViewById(R.id.badgeview_image);
            PhotosAdapter photosAdapter = (PhotosAdapter) photosList.getAdapter();
            if (photosAdapter == null) {
                photosAdapter = new PhotosAdapter(context, true);
                photosList.setAdapter(photosAdapter);
            }
            photosList.setNumColumns(PREVIEWS_PER_BADGE);

            photosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int p, long id) {
                    AlbumsFragment.this.onItemClick(null, null, position, id);
                }
            });

            TextView title = (TextView) view.findViewById(R.id.badgeview_title);
            title.setText(album.title);

            int count = 0;
            photosAdapter.clear();
            /*loop:
                for (int j = 0; j < album.chunksCount(); ++j) {
                    List<Photo> photos = album.getChunk(j);
                    for (int k = 0; k < photos.size(); ++k) {
                        ++count;
                        photosAdapter.addPhoto(photos.get(k));
                        if (count == PREVIEWS_PER_BADGE) {
                            break loop;
                        }
                    }
                }*/
            photosAdapter.notifyDataSetChanged();
            return view;
        }
    }

    private GridView albumsList;
    protected final static int PREVIEWS_PER_BADGE = 3;
    private AlbumsOwner currentKeeper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle.getString("uid") != null) {
            currentKeeper = User.get(bundle.getString("uid", ""));
        } else {
            currentKeeper = Group.get(bundle.getString("gid", ""));
        }
        assert (currentKeeper != null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.badgesactivity, container, false);
        albumsList = (GridView) view.findViewById(R.id.badgesActivity_grid);
        albumsList.setOnItemClickListener(this);
        albumsList.addOnLayoutChangeListener(this);
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
            albumsList.setNumColumns(columns);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), AlbumActivity.class);
        Album obj = (Album) albumsList.getItemAtPosition(position);
        intent.putExtra("aid", obj.getId());
        startActivity(intent);
    }

    @Override
    public void onVisible() {
        super.onVisible();
        AlbumsAdapter albumsAdapter = (AlbumsAdapter) albumsList.getAdapter();
        if (albumsAdapter == null) {
            albumsAdapter = new AlbumsAdapter(getActivity());
            albumsList.setAdapter(albumsAdapter);
        }
        albumsAdapter.clear();
        List<Album> albums = currentKeeper.getAlbums();
        for (Album album : albums) {
            albumsAdapter.add(album);
        }
        albumsAdapter.notifyDataSetChanged();
    }
}

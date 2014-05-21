package ru.example.PhotoStream.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Activities.PhotoActivity;
import ru.example.PhotoStream.Loaders.AlbumsLoader;
import ru.example.PhotoStream.Loaders.FriendsLoader;
import ru.example.PhotoStream.Loaders.GroupsLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;

public class StreamFragment extends Fragment implements IEventHadler, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener, View.OnLayoutChangeListener, AdapterView.OnItemClickListener {

    static class PhotosAdapter extends BaseAdapter {

        private List<Photo> photos = new ArrayList<>();
        private LayoutInflater inflater;

        public PhotosAdapter(Context context) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addPhoto(Photo photo) {
            photos.add(photo);
        }

        public void clear() {
            photos.clear();
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int position) {
            return photos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        static class ViewHolder {
            SmartImage image;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Photo photo = photos.get(position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.streamphotoview, parent, false);
                holder = new ViewHolder();
                holder.image = (SmartImage) convertView.findViewById(R.id.streamphotoview_imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.image.setImageBitmap(null);
            }
            holder.image.loadFromURL(photo.pic180min);
            return convertView;
        }
    }

    private Odnoklassniki api;
    private GridView photoList;
    private SwipeRefreshLayout swipeLayout;
    private Feed feed;
    private AlbumsKeeper entry;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Console.print("onActivityCreated");
        api = Odnoklassniki.getInstance(getActivity());

        feed = new Feed(api);
        feed.addEventListener(this);

        swipeLayout.setRefreshing(true);

        Bundle bundle = getArguments();
        if (bundle == null) {
            entry = User.get("");
            GroupsLoader groupsLoader = new GroupsLoader(api);
            groupsLoader.addEventListener(this);
            groupsLoader.execute();

            FriendsLoader friendsLoader = new FriendsLoader(api);
            friendsLoader.addEventListener(this);
            friendsLoader.execute();
        } else {
            if (bundle.getString("uid") != null) {
                entry = User.get(bundle.getString("uid", ""));
            } else {
                entry = Group.get(bundle.getString("gid", ""));
            }
            AlbumsLoader loader = new AlbumsLoader(api, entry);
            loader.addEventListener(this);
            loader.execute();
        }

        assert (entry != null);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        PhotoActivity.setFeed(feed);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void loadMorePhotos() {
        if (swipeLayout.isRefreshing()) {
            return;
        }
        swipeLayout.setRefreshing(true);
        feed.loadMore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Console.print("onCreateView");
        View view = inflater.inflate(R.layout.substreamactivity, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        photoList = (GridView) view.findViewById(R.id.substreamactivity_photolist);
        photoList.addOnLayoutChangeListener(this);
        photoList.setOnScrollListener(this);
        photoList.setOnItemClickListener(this);
        return view;
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Console.print("onLayoutChange");
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;
        int currentWidth = right - left;
        int currentHeight = bottom - top;
        if (oldWidth != currentWidth || oldHeight != currentHeight) {
            int columns = (int)Math.ceil(currentWidth / 180.0);
            photoList.setNumColumns(columns);
        }
    }

    private int scrollState = SCROLL_STATE_IDLE;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount && scrollState != SCROLL_STATE_IDLE) {
            loadMorePhotos();
        }
    }

    @Override
    public void onRefresh() {
        feed.resetList();
        feed.loadMore();
    }

    private int semaphore = 0;

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.GROUPS_LOADED) {
            e.target.removeEventListener(this);
            List<Group> groups = (List<Group>) e.data.get("groups");
            semaphore += groups.size();

            for (Group group : groups) {
                AlbumsLoader loader = new AlbumsLoader(api, group);
                loader.addEventListener(this);
                loader.execute();
            }
        } else if (e.type == Event.FRIENDS_LOADED) {
            e.target.removeEventListener(this);
            List<User> friends = (List<User>) e.data.get("friends");
            semaphore += friends.size();

            for (User user : friends) {
                AlbumsLoader loader = new AlbumsLoader(api, user);
                loader.addEventListener(this);
                loader.execute();
            }
        } else if (e.type == Event.ALBUMS_LOADED) {
            e.target.removeEventListener(this);
            Console.print("Albums");
            List<Album> albums = (List<Album>) e.data.get("albums");
            for (Album album : albums) {
                Console.print("Album: " + album.title + " " + album.hasMore());
                feed.add(album);
            }

            if (semaphore > 0) {
                semaphore--;
            }

            if (semaphore == 0) {
                swipeLayout.setRefreshing(false);
                loadMorePhotos();
            }
        } else if (e.type == Event.COMPLETE) {
            swipeLayout.setRefreshing(false);
            List<Photo> photos = feed.getAvailablePhotos();

            PhotosAdapter photoListAdapter = (PhotosAdapter) photoList.getAdapter();
            if (photoListAdapter == null) {
                photoListAdapter = new PhotosAdapter(getActivity());
                photoList.setAdapter(photoListAdapter);
            }
            Console.print("Total photos: " + photos.size());
            if (photos.size() > photoListAdapter.getCount()) {
                photoListAdapter.clear();

                for (int i = 0; i < photos.size(); ++i) {
                    photoListAdapter.addPhoto(photos.get(i));
                }

                photoListAdapter.notifyDataSetChanged();
            }
        }
    }
}

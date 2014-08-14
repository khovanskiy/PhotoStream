package ru.example.PhotoStream.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import ru.example.PhotoStream.Activities.PhotoActivity;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.ViewAdapters.PhotosAdapter;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;

public class StreamFragment extends IFragmentSwitcher implements IEventHadler, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener, View.OnLayoutChangeListener, AdapterView.OnItemClickListener {

    private Odnoklassniki api;
    private GridView photoList;
    private SwipeRefreshLayout swipeLayout;
    private Feed feed;
    private boolean updating = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        api = Odnoklassniki.getInstance(getActivity());

        feed = new Feed(api);
        feed.addEventListener(this);

        Bundle bundle = getArguments();
        if (bundle == null) {
            List<User> users = User.getAllUsers();
            for (User user : users) {
                feed.addAll(user.getAlbums());
            }
            List<Group> groups = Group.getAllGroups();
            for (Group group : groups) {
                feed.addAll(group.getAlbums());
            }
        } else if (bundle.getString("aid") != null) {
            feed.add(Album.get(bundle.getString("aid", "")));
        } else if (bundle.getString("uid") != null) {
            feed.addAll(User.get(bundle.getString("uid", "")).getAlbums());
        } else {
            feed.addAll(Group.get(bundle.getString("gid", "")).getAlbums());
        }

        loadMorePhotos();
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
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;
        int currentWidth = right - left;
        int currentHeight = bottom - top;
        if (oldWidth != currentWidth || oldHeight != currentHeight) {
            int columns = (int) Math.ceil(currentWidth / 180.0);
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
        feed.clear();
        feed.loadMore();
        updating = true;
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            swipeLayout.setRefreshing(false);
            List<Photo> photos = feed.getAvailablePhotos();

            PhotosAdapter photoListAdapter = (PhotosAdapter) photoList.getAdapter();
            if (photoListAdapter == null) {
                photoListAdapter = new PhotosAdapter(getActivity());
                photoList.setAdapter(photoListAdapter);
            }
            //Console.print("Total photos: " + photos.size());
            if (photos.size() > photoListAdapter.getCount() || updating) {
                photoListAdapter.clear();

                for (int i = 0; i < photos.size(); ++i) {
                    photoListAdapter.addPhoto(photos.get(i));
                }

                photoListAdapter.notifyDataSetChanged();
            }

            updating = false;
        }
    }
}

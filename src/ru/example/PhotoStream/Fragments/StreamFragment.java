package ru.example.PhotoStream.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import ru.example.PhotoStream.Activities.PhotoActivity;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Loaders.AlbumsLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class StreamFragment extends IFragmentSwitcher implements IEventHadler, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener, View.OnLayoutChangeListener, AdapterView.OnItemClickListener {

    private class ViewHolder {
        SmartImage image;
    }

    private Odnoklassniki api;
    private ArrayAdapter<Photo> photoListAdapter;
    private GridView photosGrid;
    private SwipeRefreshLayout swipeLayout;
    private Feed feed;
    private boolean updating = false;
    private int targetSize;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setRetainInstance(true);

        api = Odnoklassniki.getInstance(getActivity());

        photoListAdapter = new ArrayAdapter<Photo>(getActivity(), R.layout.streamphotoview) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Photo photo = getItem(position);
                ViewHolder holder;
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.streamphotoview, parent, false);
                    holder = new ViewHolder();
                    holder.image = (SmartImage) convertView.findViewById(R.id.streamphotoview_imageView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                    //holder.image.setImageBitmap(null);
                }
                if (photo.hasAnySize()) {
                    holder.image.loadFromURL(photo.findBestSize(targetSize, targetSize).getUrl());
                }
                return convertView;
            }
        };

        photosGrid.setAdapter(photoListAdapter);

        feed = new Feed(api);
        feed.addEventListener(this);

        Bundle bundle = getArguments();
        MultiTask<String> service = new MultiTask<String>() {
            @Override
            protected void onPostExecute(Map<String, Future<?>> data) {
                for (Map.Entry<String, Future<?>> entry : data.entrySet()) {
                    Future<List<Album>> futureAlbums = (Future<List<Album>>) entry.getValue();
                    try {
                        feed.addAll(futureAlbums.get());
                    } catch (Exception e) {
                    }
                }
                loadMorePhotos();
            }
        };
        if (bundle == null) {
            List<User> users = User.getAllUsers();
            for (User user : users) {
                service.put(user.getId(), new AlbumsLoader(api, user));
            }
            List<Group> groups = Group.getAllGroups();
            for (Group group : groups) {
                service.put(group.getId(), new AlbumsLoader(api, group));
            }
        } else if (bundle.getString("aid") != null) {
            feed.add(Album.get(bundle.getString("aid", "")));
        } else if (bundle.getString("fid") != null) {
            User user = User.get(bundle.getString("fid", ""));
            Console.print("Loading: " + user.getName() + " " + user.getId());
            service.put(user.getId(), new AlbumsLoader(api, user));
        } else {
            Group group = Group.get(bundle.getString("gid", ""));
            service.put(group.getId(), new AlbumsLoader(api, group));
        }
        swipeLayout.setRefreshing(true);
        service.execute();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), PhotoActivity.class);
        PhotoActivity.setFeed(feed);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void loadMorePhotos() {
        if (updating) {
            return;
        }
        swipeLayout.setRefreshing(true);
        updating = true;
        feed.loadMore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.substreamactivity, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.black, android.R.color.white, android.R.color.black, android.R.color.white);
        photosGrid = (GridView) view.findViewById(R.id.substreamactivity_photolist);
        photosGrid.addOnLayoutChangeListener(this);
        photosGrid.setOnScrollListener(this);
        photosGrid.setOnItemClickListener(this);
        return view;
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int oldWidth = oldRight - oldLeft;
        int oldHeight = oldBottom - oldTop;
        int currentWidth = right - left;
        int currentHeight = bottom - top;
        if (oldWidth != currentWidth || oldHeight != currentHeight) {
            targetSize = currentWidth / photosGrid.getNumColumns();
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
        loadMorePhotos();
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            swipeLayout.setRefreshing(false);
            List<Photo> photos = feed.getAvailablePhotos();

            Console.print("Total photos: " + photos.size() + " " + photoListAdapter.getCount() + " " + updating);
            if (photos.size() > photoListAdapter.getCount() || updating) {
                photoListAdapter.clear();
                photoListAdapter.addAll(photos);
                photoListAdapter.notifyDataSetChanged();
            }

            updating = false;
        }
    }
}

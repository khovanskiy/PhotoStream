package ru.example.PhotoStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StreamFragment extends Fragment implements IEventHadler, SwipeRefreshLayout.OnRefreshListener {

    static class PhotosAdapter extends BaseAdapter{

        private List<Photo> photos = new ArrayList<>();
        private Context context;

        public PhotosAdapter(Context context)
        {
             this.context = context;
        }

        public void addPhoto(Photo photo)
        {
            photos.add(photo);
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Console.print("get view " + position + " " + photos.size());
            Photo photo = photos.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.streamphotoview, parent, false);
            SmartImage imageView = (SmartImage) view.findViewById(R.id.streamphotoview_imageView);
            imageView.loadFromURL(photo.pic190x190);
            return view;
        }
    }
    private Odnoklassniki api;
    private GridView photoList;
    private PhotosAdapter photoListAdapter;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = Odnoklassniki.getInstance(getActivity());

        DataLoader loader = new DataLoader(api);
        loader.addEventListener(this);
        loader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.substreamactivity, container, false);
        photoList = (GridView) view.findViewById(R.id.substreamactivity_photolist);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return view;
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE)
        {
            List<Photo> photos = (List<Photo>) e.data.get("photos");
            Console.print("Complete loader");
            photoListAdapter = new PhotosAdapter(getActivity());
            photoList.setAdapter(photoListAdapter);

            for (Photo photo : photos)
            {
                Console.print(photo.id + " " + photo.pic640x480);
                photoListAdapter.addPhoto(photo);
            }

            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    photoListAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}

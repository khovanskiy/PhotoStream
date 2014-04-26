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
import ru.example.PhotoStream.Loaders.PhotosLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StreamFragment extends Fragment implements IEventHadler, SwipeRefreshLayout.OnRefreshListener {

    static class PhotosAdapter extends BaseAdapter{

        private List<Photo> photos = new ArrayList<>();
        private LayoutInflater inflater;

        public PhotosAdapter(Context context)
        {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.image.loadFromURL(photo.pic180min);
            return convertView;
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
                                /*50582132228315 Одноклассники. Всё ОК!
04-27 00:11:59.390: INFO/CONSOLE(20471): 53053217505400 Mobile Arena
04-27 00:11:59.490: INFO/CONSOLE(20471): 53038939046008 Одноклассники API
04-27 00:11:59.490: INFO/CONSOLE(20471): 53122247360638 Фотострим ОК*/
        DataLoader loader = new PhotosLoader(api, null, 0);
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
            e.target.removeEventListener(this);
            List<Photo> photos = (List<Photo>) e.data.get("photos");
            photoListAdapter = new PhotosAdapter(getActivity());
            photoList.setAdapter(photoListAdapter);

            for (Photo photo : photos)
            {
                photoListAdapter.addPhoto(photo);
            }

            photoListAdapter.notifyDataSetChanged();
        }
    }
}

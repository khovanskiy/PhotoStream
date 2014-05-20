package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Loaders.PhotosLoader;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.List;


public class PhotoActivity extends ActionBarActivity implements IEventHadler {

    private class FullScreenImageAdapter extends PagerAdapter {

        private List<Photo> photos = new ArrayList<>();
        private LayoutInflater inflater;

        public FullScreenImageAdapter(Context context) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void add(Photo photo) {
            photos.add(photo);
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Photo photo = photos.get(position);
            View viewLayout = inflater.inflate(R.layout.photoactivity_page, container, false);
            SmartImage imgDisplay = (SmartImage) viewLayout.findViewById(R.id.photoactivity_page_image);
            imgDisplay.loadFromURL(photo.pic1024x768);
            container.addView(viewLayout);
            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public void clear() {
            photos.clear();
        }
    }

    private static Feed feed;
    private Odnoklassniki api;
    private ViewPager viewPager;
    private int initPosition;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoactivity);

        api = Odnoklassniki.getInstance(this);

        initPosition = getIntent().getIntExtra("position", 0);
        viewPager = (ViewPager) findViewById(R.id.photoactivity_pager);
        FullScreenImageAdapter photoListAdapter = new FullScreenImageAdapter(this);
        viewPager.setAdapter(photoListAdapter);
        List<Photo> photos = feed.getAvailablePhotos();
        for (Photo photo : photos) {
            photoListAdapter.add(photo);
        }
        viewPager.setCurrentItem(initPosition);
        photoListAdapter.notifyDataSetChanged();
    }

    public static void setFeed(Feed newFeed) {
        feed = newFeed;
        assert (feed != null);
    }

    private void loadMore() {
        DataLoader loader = new PhotosLoader(api, feed);
        loader.addEventListener(this);
        loader.execute();
    }

    @Override
    public void handleEvent(Event e) {
        /*if (e.type == Event.COMPLETE) {
            e.target.removeEventListener(this);
            List<Photo> photos = (List<Photo>) e.data.get("photos");
            FullScreenImageAdapter photoListAdapter = (FullScreenImageAdapter) viewPager.getAdapter();
            boolean isInit = photoListAdapter.getCount() == 0;

            if (photos.size() > photoListAdapter.getCount()) {
                photoListAdapter.clear();

                for (Photo photo : photos) {
                    photoListAdapter.add(photo);
                }

                photoListAdapter.notifyDataSetChanged();
            }

            if (isInit) {
                viewPager.setCurrentItem(initPosition);
            }
        } */
    }
}
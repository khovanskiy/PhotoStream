package ru.example.PhotoStream.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Fragments.PhotoFragment;

import java.util.List;
import java.util.Map;

public class PhotoActivity extends UIActivity implements ViewPager.OnPageChangeListener, IEventHandler {

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (photos.size() == position + 1) {
            feed.loadMore();
        }
        Photo photo = photos.get(position);
        Album album = Album.get(photo.album_id);
        if (album.albumType == AlbumType.USER) {
            User user = User.get(album.user_id);
            this.setTitle(user.name + " " + album.title);
        } else {
            Group group = Group.get(album.group_id);
            this.setTitle(group.name + " " + album.title);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
        if (type.equals(Event.COMPLETE)) {
            photoListAdapter.notifyDataSetChanged();
            if (initPosition == -1) {
                initPosition = 0;
                viewPager.setCurrentItem(initPosition);
            }
        }
    }

    private class PageAdapter extends FragmentStatePagerAdapter {

        private final IEventHandler photoLoadedHandler = new IEventHandler() {
            @Override
            public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
                if (type.equals(Event.CREATE)) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (type.equals(Event.COMPLETE)) {
                    progressBar.setVisibility(View.GONE);
                } else if (type.equals("hideUI")) {
                    getSupportActionBar().hide();
                } else if (type.equals("showUI")) {
                    getSupportActionBar().show();
                }
            }
        };
        public PageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Photo photo = photos.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("photoId", photo.id);
            PhotoFragment photoFragment = new PhotoFragment();
            photoFragment.addEventListener(photoLoadedHandler);
            photoFragment.setArguments(bundle);
            return photoFragment;
        }

        @Override
        public int getCount() {
            return photos.size();
        }
    }

    private static Feed feed;
    private int initPosition;
    protected List<Photo> photos;
    protected PageAdapter photoListAdapter;
    protected ViewPager viewPager;
    protected ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.photoactivity);
        System.out.println("OnCreate");

        progressBar = (ProgressBar) findViewById(R.id.photoactivity_progress);
        viewPager = (ViewPager) findViewById(R.id.photoactivity_pager);

        feed.addEventListener(this);
        photos = feed.getAvailablePhotos();

        photoListAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(photoListAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey("position")) {
            initPosition = savedInstanceState.getInt("position");
        } else {
            initPosition = getIntent().getIntExtra("position", 0);
        }
        viewPager.setOnPageChangeListener(this);
        if (initPosition != -1) {
            viewPager.setCurrentItem(initPosition);
        } else {
            feed.loadMore();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static void setFeed(Feed newFeed) {
        feed = newFeed;
        assert (feed != null);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstance) {
        saveInstance.putInt("position", viewPager.getCurrentItem());
    }

}
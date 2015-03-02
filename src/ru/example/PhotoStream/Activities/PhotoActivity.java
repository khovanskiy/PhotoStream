package ru.example.PhotoStream.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
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
        mPreviousPosition = mCurrentPosition;
        if (mPreviousPosition != -1) {
            PhotoFragment fragment = (PhotoFragment) photoListAdapter.getRegisteredFragment(mPreviousPosition);
            if (fragment != null) {
                fragment.restoreZoom();
            }
        }
        mCurrentPosition = position;
        if (photos.size() == position + 1) {
            feed.fetch();
            return;
        }
        updateUI(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateUI(int position) {
        Photo photo = photos.get(position);
        TextView actionBarTitle = (TextView) findViewById(R.id.photoactivity_actionbar_title);
        TextView actionBarSubtitle = (TextView) findViewById(R.id.photoactivity_actionbar_subtitle);
        Album album = Album.get(photo.album_id);
        if (album.albumType == AlbumType.USER) {
            User user = User.get(album.user_id);
            actionBarTitle.setText(user.name);
        } else {
            Group group = Group.get(album.group_id);
            actionBarTitle.setText(group.name);
        }
        actionBarSubtitle.setText(album.title);
        PhotoFragment photoFragment = (PhotoFragment) photoListAdapter.getRegisteredFragment(position);
        if (photoFragment != null) {
            photoFragment.toggleUI(hiddenUI);
        }
    }

    @Override
    public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
        if (type.equals(Event.COMPLETE)) {
            photoListAdapter.notifyDataSetChanged();
            if (mCurrentPosition == -1) {
                mCurrentPosition = 0;
                viewPager.setCurrentItem(mCurrentPosition);
            }
            updateUI(mCurrentPosition);
        }
    }

    private class PageAdapter extends FragmentStatePagerAdapter {
        private final SparseArray<Fragment> registeredFragments = new SparseArray<>();

        private final IEventHandler photoLoadedHandler = new IEventHandler() {
            @Override
            public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
                if (type.equals(Event.CREATE)) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (type.equals(Event.COMPLETE)) {
                    progressBar.setVisibility(View.GONE);
                } else if (type.equals("hideUI")) {
                    hiddenUI = true;
                    getSupportActionBar().hide();
                } else if (type.equals("showUI")) {
                    hiddenUI = false;
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
            bundle.putBoolean("hiddenUI", hiddenUI);
            PhotoFragment photoFragment = new PhotoFragment();
            photoFragment.addEventListener(photoLoadedHandler);
            photoFragment.setArguments(bundle);
            return photoFragment;
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    private Feed feed;
    private int mCurrentPosition;
    private int mPreviousPosition;
    protected List<Photo> photos;
    protected PageAdapter photoListAdapter;
    protected ViewPager viewPager;
    protected ProgressBar progressBar;
    protected boolean hiddenUI;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.photo_activity_actionbar, null));
        setContentView(R.layout.photoactivity);

        feed = (Feed) weakCache(PhotoActivity.class).get("feed");
        if (feed == null) {
            finish();
            return;
        }

        mPreviousPosition = -1;
        if (savedInstanceState != null && savedInstanceState.containsKey("position")) {
            mCurrentPosition = savedInstanceState.getInt("position");
        } else {
            mCurrentPosition = getIntent().getIntExtra("position", 0);
        }

        progressBar = (ProgressBar) findViewById(R.id.photoactivity_progress);
        viewPager = (ViewPager) findViewById(R.id.photoactivity_pager);

        TextView actionBarTitle = (TextView) findViewById(R.id.photoactivity_actionbar_title);
        actionBarTitle.setText(feed.getName());

        feed.addEventListener(this);
        photos = feed.getAvailablePhotos();

        photoListAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(photoListAdapter);


        viewPager.setOnPageChangeListener(this);
        if (mCurrentPosition != -1) {
            viewPager.setCurrentItem(mCurrentPosition);
        } else {
            feed.fetch();
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

    @Override
    public void onSaveInstanceState(Bundle saveInstance) {
        saveInstance.putInt("position", viewPager.getCurrentItem());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weakCache(PhotoActivity.class).remove("feed");
    }
}
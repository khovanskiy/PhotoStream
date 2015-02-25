package ru.example.PhotoStream.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import net.hockeyapp.android.CrashManager;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Fragments.PhotoFragment;
import ru.ok.android.sdk.Odnoklassniki;
import uk.co.senab.photoview.HackyViewPager;

import java.util.List;
import java.util.Map;


public class PhotoActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener, IEventHandler {

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //Console.print("Page selected " + photos.size() + " " + position);
        if (photos.size() == position + 1) {
            feed.loadMore();
        }
        /*Photo photo = photos.get(i);
        Album album = Album.get(photo.album_id);
        if (album.albumType == AlbumType.USER) {
            User user = User.get(album.user_id);
            this.setTitle(user.name + " " + album.title);
        } else {
            Group group = Group.get(album.group_id);
            this.setTitle(group.name + " " + album.title);
        }*/
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void handleEvent(IEventDispatcher dispatcher, String type, Map<String, Object> data) {
        if (type == Event.COMPLETE) {
            photoListAdapter.notifyDataSetChanged();
            if (initPosition == -1) {
                initPosition = 0;
                viewPager.setCurrentItem(initPosition);
                progressBar.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                frameLayout.bringChildToFront(viewPager);
            }
        }
    }

    private class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Photo photo = photos.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("photoId", photo.id);
            PhotoFragment fr = new PhotoFragment();
            fr.setViewPagerLockListener(new PhotoFragment.OnViewPagerLock() {
                @Override
                public void setLocked(boolean isLocked) {
                    viewPager.setLocked(isLocked);
                }
            });
            fr.setArguments(bundle);
            return fr;
        }

        @Override
        public int getCount() {
            return photos.size();
        }
    }

    private static Feed feed;
    private int initPosition;
    protected Odnoklassniki api;
    protected List<Photo> photos;
    protected PageAdapter photoListAdapter;
    protected HackyViewPager viewPager;
    protected FrameLayout frameLayout;
    protected ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoactivity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        api = Odnoklassniki.getInstance(this);
        frameLayout = (FrameLayout) findViewById(R.id.photoactivity_frame);
        progressBar = (ProgressBar) findViewById(R.id.photoactivity_progress);
        viewPager = (HackyViewPager) findViewById(R.id.photoactivity_pager);

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
            progressBar.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            frameLayout.bringChildToFront(viewPager);
        } else {
            feed.loadMore();
        }
        getSupportActionBar().hide();
    }

    public static void setFeed(Feed newFeed) {
        feed = newFeed;
        assert (feed != null);
    }

    @Override
    public void onResume() {
        super.onResume();
        CrashManager.register(this, "5adb6faead01ccaa24e6865215ddcb59");
    }

    @Override
    public void onBackPressed() {
        SmartImage.clearCache();
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstance) {
        saveInstance.putInt("position", viewPager.getCurrentItem());
    }

}
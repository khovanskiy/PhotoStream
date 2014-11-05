package ru.example.PhotoStream.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Fragments.PhotoFragment;
import ru.ok.android.sdk.Odnoklassniki;
import uk.co.senab.photoview.HackyViewPager;

import java.util.List;


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
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE) {
            photoListAdapter.notifyDataSetChanged();
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
    protected Odnoklassniki api;
    protected List<Photo> photos;
    protected PageAdapter photoListAdapter;
    protected HackyViewPager viewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoactivity);
        api = Odnoklassniki.getInstance(this);
        feed.addEventListener(this);
        photos = feed.getAvailablePhotos();

        int initPosition = getIntent().getIntExtra("position", 0);
        viewPager = (HackyViewPager) findViewById(R.id.photoactivity_pager);
        photoListAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(photoListAdapter);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(initPosition);
    }

    public static void setFeed(Feed newFeed) {
        feed = newFeed;
        assert (feed != null);
    }
}
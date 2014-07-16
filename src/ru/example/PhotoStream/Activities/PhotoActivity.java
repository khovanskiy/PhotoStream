package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Fragments.FriendsFragment;
import ru.example.PhotoStream.Fragments.GroupsFragment;
import ru.example.PhotoStream.Fragments.PhotoFragment;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhotoActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        Photo photo = photos.get(i);
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
    public void onPageScrollStateChanged(int i) {

    }

    private class PageAdapter extends FragmentPagerAdapter {

        public PageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Photo photo = photos.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("photoId", photo.id);
            Fragment fr = new PhotoFragment();
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoactivity);
        api = Odnoklassniki.getInstance(this);
        photos = feed.getAvailablePhotos();

        int initPosition = getIntent().getIntExtra("position", 0);
        ViewPager viewPager = (ViewPager) findViewById(R.id.photoactivity_pager);
        PageAdapter photoListAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(photoListAdapter);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(initPosition);
    }

    public static void setFeed(Feed newFeed) {
        feed = newFeed;
        assert (feed != null);
    }
}
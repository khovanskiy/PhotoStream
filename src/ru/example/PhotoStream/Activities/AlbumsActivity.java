package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import ru.example.PhotoStream.Fragments.AlbumsFragment;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.example.PhotoStream.Group;
import ru.example.PhotoStream.IFragmentSwitcher;
import ru.example.PhotoStream.R;
import ru.example.PhotoStream.User;


public class AlbumsActivity extends ActionBarActivity {

    private class PageAdapter extends FragmentPagerAdapter {
        private Context context;

        public PageAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
            fragments[0] = new StreamFragment();
            titles[0] = R.string.photoStream;
            fragments[1] = new AlbumsFragment();
            titles[1]  = R.string.albums;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position % MAX_PAGES];
        }

        @Override
        public int getCount() {
            return MAX_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return context.getString(titles[position % MAX_PAGES]);
        }
    }

    protected final static int MAX_PAGES = 2;
    protected final static int DEFAULT_PAGE_ID = 0;
    protected final IFragmentSwitcher[] fragments = new IFragmentSwitcher[MAX_PAGES];
    protected final int[] titles = new int[MAX_PAGES];

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsactivity);

        ViewPager pager = (ViewPager) findViewById(R.id.albumsctivity_pager);
        FragmentPagerAdapter adapter = new PageAdapter(getSupportFragmentManager(), this);
        PagerTabStrip tabStrip = (PagerTabStrip) findViewById(R.id.albumsactivity_pagertabstrip);
        tabStrip.setTabIndicatorColor(Color.argb(0xff, 0xfd, 0x97, 0x0f));
        tabStrip.setBackgroundColor(Color.argb(0xff, 0xe0, 0xe0, 0xe0));
        tabStrip.setDrawFullUnderline(true);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tabStrip.setTextSpacing(35);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_PAGE_ID);

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        if (intent.hasExtra("uid")) {
            String uid = intent.getStringExtra("uid");
            bundle.putString("uid", uid);
            setTitle(User.get(uid).name);
        } else {
            String gid = intent.getStringExtra("gid");
            bundle.putString("gid", gid);
            setTitle(Group.get(gid).name);
        }
        fragments[0].setArguments(bundle);
        fragments[1].setArguments(bundle);
    }
}
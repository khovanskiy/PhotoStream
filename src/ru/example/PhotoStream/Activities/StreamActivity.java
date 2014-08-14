package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import ru.example.PhotoStream.Fragments.FriendsFragment;
import ru.example.PhotoStream.Fragments.GroupsFragment;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.example.PhotoStream.IFragmentSwitcher;
import ru.example.PhotoStream.R;

public class StreamActivity extends ActionBarActivity {

    private class PageAdapter extends FragmentPagerAdapter{
        private Context context;

        public PageAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
            fragments[0] = new StreamFragment();//GroupsFragment();
            titles[0] = R.string.my_groups;
            fragments[1] = new StreamFragment();
            titles[1] = R.string.photoStream;
            fragments[2] = new StreamFragment();//FriendsFragment();
            titles[2] = R.string.my_friends;
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

    protected final static int MAX_PAGES = 3;
    protected final static int DEFAULT_PAGE_ID = 1;
    protected final IFragmentSwitcher[] fragments = new IFragmentSwitcher[MAX_PAGES];
    protected final int[] titles = new int[MAX_PAGES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streamactivity);

        ViewPager pager = (ViewPager) findViewById(R.id.streamactivity_pager);
        FragmentPagerAdapter adapter = new PageAdapter(getSupportFragmentManager(), this);
        PagerTabStrip tabStrip = (PagerTabStrip) findViewById(R.id.streamactivity_pagertabstrip);
        tabStrip.setTabIndicatorColor(Color.argb(0xff, 0xfd, 0x97, 0x0f));
        tabStrip.setBackgroundColor(Color.argb(0xff, 0xe0, 0xe0, 0xe0));
        tabStrip.setDrawFullUnderline(true);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tabStrip.setTextSpacing(35);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_PAGE_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_upload:
            {
                Intent intent = new Intent(this, PhotoTakerActivity.class);
                startActivity(intent);
            } break;
        }
        return super.onOptionsItemSelected(item);
    }
}

package ru.example.PhotoStream;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import org.json.JSONObject;
import ru.example.PhotoStream.ViewAdapters.PhotoListAdapter;
import ru.ok.android.sdk.Odnoklassniki;

public class StreamActivity extends FragmentActivity implements ActionBar.TabListener, ViewPager.OnPageChangeListener, IEventHadler{

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
          //pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        //getActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void handleEvent(Event e) {
        if (e.type == Event.COMPLETE)
        {
            e.target.removeEventListener(this);
            Console.print("COMPLETE");
        }
    }

    static class PageAdapter extends FragmentPagerAdapter
    {
        private Context context;

        public PageAdapter(FragmentManager fragmentManager, Context context)
        {
            super(fragmentManager);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 1:
                {
                    return new FriendsFragment();
                }
                case 2:
                {
                    return new GroupsFragment();
                }
                default:
                {
                    return new StreamFragment();
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position)
            {
                case 1:
                {
                    return context.getString(R.string.my_friends);
                }
                case 2:
                {
                    return context.getString(R.string.my_groups);
                }
                default:
                {
                    return context.getString(R.string.my_stream);
                }
            }
        }
    }
    private final int PHOTOS_TO_LOAD = 3;
    private JSONObject[] photosToDisplay;
    private int nextAdded;
    private ListView photoList;
    private PhotoListAdapter photoListAdapter;
    private Odnoklassniki mOdnoklassniki;
    private ActionBar actionBar;
    private ViewPager pager;
    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streamactivity);

        mOdnoklassniki = Odnoklassniki.getInstance(getApplicationContext());

        actionBar = getActionBar();
        pager = (ViewPager)findViewById(R.id.streamactivity_pager);
        adapter = new PageAdapter(getSupportFragmentManager(), this);
        PagerTabStrip tabStrip = (PagerTabStrip)findViewById(R.id.streamactivity_pagertabstrip);
        tabStrip.setTabIndicatorColor(Color.argb(0xff, 0xfd, 0x97, 0x0f));
        tabStrip.setBackgroundColor(Color.argb(0xff, 0xe0, 0xe0,0xe0));
        tabStrip.setDrawFullUnderline(true);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tabStrip.setTextSpacing(35);

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.addTab(actionBar.newTab().setText(getString(R.string.my_stream)).setTabListener(this));
        //actionBar.addTab(actionBar.newTab().setText(getString(R.string.my_friends)).setTabListener(this));
        //actionBar.addTab(actionBar.newTab().setText(getString(R.string.my_groups)).setTabListener(this));

        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
        pager.setCurrentItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    private void onPhotoClick(int position) {
        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra("photo_id", photoListAdapter.getPhotoId(position));
        if (photoListAdapter.getFriendId(position) != null) {
            intent.putExtra("fid", photoListAdapter.getFriendId(position));
        }
        if (photoListAdapter.getGroupId(position) != null) {
            intent.putExtra("gid", photoListAdapter.getGroupId(position));
        }
        if (photoListAdapter.getAlbumId(position) != null) {
            intent.putExtra("aid", photoListAdapter.getAlbumId(position));
        }
        startActivity(intent);
    }

    private void loadMorePhotos() {
        for (int i = nextAdded; i < Math.min(nextAdded + PHOTOS_TO_LOAD, photosToDisplay.length); i++) {
            try {
                photoListAdapter.addPhoto(photosToDisplay[i].getString("id"));
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
        }
        nextAdded = Math.min(nextAdded + PHOTOS_TO_LOAD, photosToDisplay.length);
    }

    private void update() {
        /*photoList = (ListView) findViewById(R.id.streamactivity_photolist);
        photoList.setDividerHeight(20);
        photoListAdapter = new PhotoListAdapter(this);
        photoList.setAdapter(photoListAdapter);
        photoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPhotoClick(position);
            }
        });
        photosToDisplay = InfoHolder.sortedPhotos.toArray(new JSONObject[0]);
        nextAdded = 0;
        loadMorePhotos();
        photoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if(lastItem == totalItemCount) {
                    loadMorePhotos();
                }
            }
        }); */
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

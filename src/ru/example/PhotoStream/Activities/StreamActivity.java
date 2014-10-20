package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.example.PhotoStream.IFragmentSwitcher;
import ru.example.PhotoStream.R;

public class StreamActivity extends ActionBarActivity implements View.OnClickListener {

    private class PageAdapter extends FragmentPagerAdapter {
        private Context context;

        public PageAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
            fragments[0] = new StreamFragment();
            fragments[0].setArguments(getIntent().getExtras());
            titles[0] = R.string.photoStream;
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

    protected final static int MAX_PAGES = 1;
    protected final static int DEFAULT_PAGE_ID = 0;
    protected final IFragmentSwitcher[] fragments = new IFragmentSwitcher[MAX_PAGES];
    protected final int[] titles = new int[MAX_PAGES];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streamactivity);

        //ImageButton button = (ImageButton) findViewById(R.id.stream_activity_button);
        //button.setOnClickListener(this);

        ViewPager pager = (ViewPager) findViewById(R.id.stream_activity_pager);
        FragmentPagerAdapter adapter = new PageAdapter(getSupportFragmentManager(), this);
        /*PagerTabStrip tabStrip = (PagerTabStrip) findViewById(R.id.streamactivity_pagertabstrip);
        tabStrip.setTabIndicatorColor(Color.argb(0xff, 0xfd, 0x97, 0x0f));
        tabStrip.setBackgroundColor(Color.argb(0xff, 0xe0, 0xe0, 0xe0));
        tabStrip.setDrawFullUnderline(true);
        tabStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tabStrip.setTextSpacing(35);*/

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        pager.setAdapter(adapter);
        pager.setCurrentItem(DEFAULT_PAGE_ID);

        Intent intent = getIntent();
        if (intent.hasExtra("forwarding")) {
            Intent forwardingIntent = new Intent(this, PhotoActivity.class);
            forwardingIntent.putExtra("position", intent.getIntExtra("position", 0));
            startActivity(forwardingIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stream_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back: {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {
            case R.id.stream_activity_button: {
                Intent intent = new Intent(this, PhotoTakerActivity.class);
                startActivity(intent);
            }
            break;
        }*/
    }
}

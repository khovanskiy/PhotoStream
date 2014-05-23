package ru.example.PhotoStream.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import ru.example.PhotoStream.Fragments.FriendsFragment;
import ru.example.PhotoStream.Fragments.GroupsFragment;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.example.PhotoStream.Group;
import ru.example.PhotoStream.IFragmentSwitcher;
import ru.example.PhotoStream.R;
import ru.example.PhotoStream.User;
import ru.ok.android.sdk.Odnoklassniki;


public class AlbumsActivity extends ActionBarActivity {

    private class PageAdapter extends FragmentPagerAdapter {
        private Context context;

        public PageAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            this.context = context;
            fragments[0] = new StreamFragment();
            titles[0] = R.string.my_stream;
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
    protected final static int DEFAULT_PAGE_ID = 0;
    protected final IFragmentSwitcher[] fragments = new IFragmentSwitcher[MAX_PAGES];
    protected final int[] titles = new int[MAX_PAGES];

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsactivity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.albumsactivity_frame);
        Fragment newFragment = new StreamFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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
        newFragment.setArguments(bundle);
        ft.add(frameLayout.getId(), newFragment).commit();
    }
}
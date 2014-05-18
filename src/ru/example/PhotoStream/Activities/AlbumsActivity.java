package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.example.PhotoStream.Group;
import ru.example.PhotoStream.R;
import ru.example.PhotoStream.User;
import ru.ok.android.sdk.Odnoklassniki;


public class AlbumsActivity extends ActionBarActivity {

    private Odnoklassniki mOdnoklassniki;
    private ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsactivity);

        mOdnoklassniki = Odnoklassniki.getInstance(getApplicationContext());
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

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
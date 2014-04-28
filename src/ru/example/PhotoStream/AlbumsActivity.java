package ru.example.PhotoStream;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import ru.example.PhotoStream.ViewAdapters.AlbumListAdapter;
import ru.ok.android.sdk.Odnoklassniki;


public class AlbumsActivity extends FragmentActivity {

    private String fid, gid;
    private ListView albumList;
    private AlbumListAdapter albumListAdapter;
    private Odnoklassniki mOdnoklassniki;
    private ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albumsactivity);

        mOdnoklassniki = Odnoklassniki.getInstance(getApplicationContext());
        actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.albumsactivity_frame);
        Fragment newFragment = new StreamFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString("uid", intent.getStringExtra("uid"));
        bundle.putString("gid", intent.getStringExtra("gid"));
        newFragment.setArguments(bundle);
        ft.add(frameLayout.getId(), newFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
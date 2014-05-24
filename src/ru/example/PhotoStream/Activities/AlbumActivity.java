package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import ru.example.PhotoStream.Fragments.StreamFragment;
import ru.example.PhotoStream.R;

public class AlbumActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragmentactivity);
        Intent intent = getIntent();
        assert (intent.hasExtra("aid"));
        FrameLayout layout = (FrameLayout) findViewById(R.id.fragmentactivity_frame);
        Bundle bundle = new Bundle();
        Fragment fragment = new StreamFragment();
        bundle.putString("aid", intent.getStringExtra("aid"));
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(layout.getId(), fragment).commit();
    }
}

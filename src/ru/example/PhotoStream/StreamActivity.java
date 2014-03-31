package ru.example.PhotoStream;

import android.app.Activity;
import android.os.Bundle;
import ru.ok.android.sdk.Odnoklassniki;

import java.io.IOException;

public class StreamActivity extends Activity
{
    private Odnoklassniki mOdnoklassniki= null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streamactivity);
        mOdnoklassniki = Odnoklassniki.getInstance(getApplicationContext());
    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }
}

package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import ru.example.PhotoStream.Console;
import ru.example.PhotoStream.R;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class AuthActivity extends ActionBarActivity implements OkTokenRequestListener {
    private Odnoklassniki mOdnoklassniki = null;

    private final String APP_ID = "409574400";
    private final String APP_SECRET_KEY = "9C9616F58E44F35643492983";
    private final String APP_PUBLIC_KEY = "CBANJKGJBBABABABA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authactivity);

        mOdnoklassniki = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
        mOdnoklassniki.setTokenRequestListener(this);

        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS, OkScope.PHOTO_CONTENT);
    }

    public void onLoginClick(View view) {
        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS, OkScope.PHOTO_CONTENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSuccess(String token) {
        Intent intent = new Intent(this, InitActivity.class);
        startActivity(intent);
    }

    @Override
    public void onError() {
        Console.print("Error");
        mOdnoklassniki.removeTokenRequestListener();
        finish();
    }

    @Override
    public void onCancel() {
        Console.print("Cancel");
        mOdnoklassniki.removeTokenRequestListener();
        finish();
    }
}

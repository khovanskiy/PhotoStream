package ru.example.PhotoStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class AuthActivity extends Activity implements OkTokenRequestListener {

    private Odnoklassniki mOdnoklassniki= null;

    private final String APP_ID = "409574400";
    private final String APP_SECRET_KEY = "9C9616F58E44F35643492983";
    private final String APP_PUBLIC_KEY = "CBANJKGJBBABABABA";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authactivity);

        Console.print("Auth Activity");
        mOdnoklassniki = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);

        mOdnoklassniki.setTokenRequestListener(this);
        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS);
    }

    @Override
    protected void onDestroy() {
        mOdnoklassniki.removeTokenRequestListener();
        super.onDestroy();
    }

    @Override
    public void onSuccess(String token) {
        Console.print("Your token: " + token);
        startActivity(new Intent(AuthActivity.this, StreamActivity.class));
        finish();
    }

    @Override
    public void onError() {

    }

    @Override
    public void onCancel() {

    }
}

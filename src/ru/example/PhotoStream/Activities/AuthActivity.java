package ru.example.PhotoStream.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import ru.example.PhotoStream.R;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class AuthActivity extends Activity implements OkTokenRequestListener {
    private Odnoklassniki api = null;

    private final String APP_ID = "409574400";
    private final String APP_SECRET_KEY = "9C9616F58E44F35643492983";
    private final String APP_PUBLIC_KEY = "CBANJKGJBBABABABA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authactivity);

        api = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
        api.setTokenRequestListener(this);

        api.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS, OkScope.PHOTO_CONTENT);
    }

    public void onLoginClick(View view) {
        api.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS, OkScope.PHOTO_CONTENT);
    }

    @Override
    public void onSuccess(String token) {
        Intent intent = new Intent(this, FeedsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onError() {
        api.removeTokenRequestListener();
        finish();
    }

    @Override
    public void onCancel() {
        api.removeTokenRequestListener();
        finish();
    }
}

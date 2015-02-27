package ru.example.PhotoStream.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import ru.example.PhotoStream.R;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class AuthActivity extends UIActivity implements OkTokenRequestListener {
    private Odnoklassniki api = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authactivity);

        api = getAPI();
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

package ru.example.PhotoStream;

import android.app.Activity;
import android.os.Bundle;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class AuthActivity extends Activity implements OkTokenRequestListener {

    private Odnoklassniki mOdnoklassniki;

    private final String APP_ID = "409574400";
    private final String APP_SECRET_KEY = "CBANJKGJBBABABABA";
    private final String APP_PUBLIC_KEY = "9C9616F58E44F35643492983";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authactivity);

        Console.print("Auth Activity");
        mOdnoklassniki = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);

//определяем callback на операции с получением токена
        mOdnoklassniki.setTokenRequestListener(this);
        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS);
    }

    @Override
    protected void onDestroy() {
        mOdnoklassniki.removeTokenRequestListener();
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    public void onSuccess(String token) {
        Console.print("Your token: " + token);
    }

    @Override
    public void onError() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onCancel() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

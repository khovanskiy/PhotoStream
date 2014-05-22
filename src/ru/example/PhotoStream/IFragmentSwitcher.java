package ru.example.PhotoStream;

import android.support.v4.app.Fragment;

public abstract class IFragmentSwitcher extends Fragment {

    private boolean visible;

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible && !visible) {
            visible = true;
            onVisible();
        } else if (!menuVisible && visible) {
            visible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
    }

    protected void onInvisible() {
    }
}

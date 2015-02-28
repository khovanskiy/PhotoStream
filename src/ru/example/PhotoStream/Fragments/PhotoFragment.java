package ru.example.PhotoStream.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.example.PhotoStream.*;
import ru.example.PhotoStream.Activities.UIActivity;

import java.util.*;

public class PhotoFragment extends Fragment implements View.OnClickListener, IEventDispatcher {

    private final EventDispatcher dispatcher = new EventDispatcher();

    @Override
    public void addEventListener(IEventHandler listener) {
        dispatcher.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventHandler listener) {
        dispatcher.removeEventListener(listener);
    }

    @Override
    public void dispatchEvent(String type, Map<String, Object> data) {
        dispatcher.dispatchEvent(type, data);
    }

    private class LikeAdder extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Map<String, String> requestParams = new HashMap<>();
                requestParams.put("photo_id", photo.id);
                Album album = Album.get(photo.album_id);
                if (album.albumType == AlbumType.GROUP) {
                    requestParams.put("gid", album.group_id);
                }
                String responseString = UIActivity.getAPI().request("photos.addPhotoLike", requestParams, "get");
                //Console.print("Like response: " + responseString);
                if (responseString.equals("true")) {
                    return true;
                } else {
                    Console.print(responseString);
                    return false;
                }
            } catch (Exception e) {
                Log.i("CONSOLE", "Error", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            photo.liked_it = true;
            photo.like_count++;
            likeButton.setImageResource(R.drawable._0001_big_liked);
            likeButton.setEnabled(false);
            likesCount.setText(photo.like_count + "");
        }
    }

    protected Photo photo;
    protected ImageButton likeButton;
    protected TextView likesCount;
    protected ZoomView zoomView;
    protected SmartImage image;
    protected boolean hiddenUI;

    public void toggleUI(boolean hidden) {
        hiddenUI = hidden;
        if (!hiddenUI) {
            likeButton.setVisibility(View.VISIBLE);
            likesCount.setVisibility(View.VISIBLE);
        } else {
            likesCount.setVisibility(View.GONE);
            likeButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*if (savedInstanceState != null && savedInstanceState.containsKey("hiddenUI")) {
            hiddenUI = savedInstanceState.getBoolean("hiddenUI");
        }*/
        Bundle args = getArguments();
        photo = Photo.get(args.getString("photoId"));
        hiddenUI = args.getBoolean("hiddenUI");

        View viewLayout = inflater.inflate(R.layout.photoactivity_page, container, false);
        /*Date now = new Date();
        long diff = (now.getTime() - photo.created_ms) / 1000;

        TextView dateTime = (TextView) viewLayout.findViewById(R.id.photoactivity_page_datetime);
        if (diff <= 5) {
            dateTime.setText("только что");
        } else if (diff < 60) {
            dateTime.setText(diff + " c.");
        } else if (diff < 3600) {
            dateTime.setText((diff / 60) + " м.");
        } else if (diff < 86400) {
            dateTime.setText((diff / 3600) + " ч.");
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(photo.created_ms);
            dateTime.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR));
        }
        if (photo.text.isEmpty()) {
            ScrollView descriptionScroll = (ScrollView) viewLayout.findViewById(R.id.photoactivity_page_description_scroll);
            descriptionScroll.setVisibility(View.GONE);
        } else {
            TextView description = (TextView) viewLayout.findViewById(R.id.photoactivity_page_description);
            description.setText(photo.text);
        }/**/
        zoomView = (ZoomView) viewLayout.findViewById(R.id.photoactivity_zoom_view);

        /*View hoverArea = viewLayout.findViewById(R.id.photoactivity_area);
        hoverArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                return true;
            }
        });*/



        likesCount = (TextView) viewLayout.findViewById(R.id.photoactivity_page_likescount);
        likesCount.setText(photo.like_count + "");

        image = (SmartImage) viewLayout.findViewById(R.id.photoactivity_page_image);
        image.setOnSmartViewLoadedListener(new SmartImage.SmartViewLoadedListener() {
            @Override
            public void onUpdated() {
                image.setVisibility(View.VISIBLE);
                dispatchEvent(Event.COMPLETE, null);
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleUI(!hiddenUI);
                if (!hiddenUI) {
                    dispatchEvent("showUI", null);
                } else {
                    dispatchEvent("hideUI", null);
                }
            }
        });
        image.setVisibility(View.GONE);
        dispatchEvent(Event.CREATE, null);
        image.setImageURL(photo.getMaxSize().getUrl());

        likeButton = (ImageButton) viewLayout.findViewById(R.id.photoactivity_page_like);
        if (photo.user_id.equals(User.currentUID)) {
            likeButton.setEnabled(false);
            likeButton.setImageResource(R.drawable._0001_big_liked);
        } else {
            likeButton.setOnClickListener(this);
            if (photo.liked_it) {
                likeButton.setImageResource(R.drawable._0001_big_liked);
                likeButton.setEnabled(false);
            }
        }

        toggleUI(hiddenUI);
        return viewLayout;
    }

    public void restoreZoom() {
        zoomView.smoothZoomTo(1, 0, 0);
        zoomView.invalidate();
    }

    @Override
    public void onClick(View v) {
        if (!photo.liked_it) {
            LikeAdder adder = new LikeAdder();
            adder.execute();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hiddenUI", hiddenUI);
    }
}

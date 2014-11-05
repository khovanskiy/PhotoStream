package ru.example.PhotoStream.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.util.*;

public class PhotoFragment extends Fragment implements View.OnClickListener, SmartImage.OnSmartViewLoadedListener {

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
                String responseString = api.request("photos.addPhotoLike", requestParams, "get");
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

    protected Odnoklassniki api;
    protected Photo photo;
    protected ImageButton likeButton;
    protected TextView likesCount;
    protected ProgressBar progressBar;
    protected SmartImage image;
    protected boolean state = true;
    protected PhotoViewAttacher photoViewAttacher;
    protected OnViewPagerLock onViewPagerLock;

    private synchronized void checkState(final View viewLayout) {
        LinearLayout header = (LinearLayout) viewLayout.findViewById(R.id.photoactivity_page_full_header);
        if (state) {
            header.setVisibility(View.VISIBLE);
            if (photoViewAttacher != null) {
                photoViewAttacher.setZoomable(true);
                photoViewAttacher.cleanup();
            }
            onViewPagerLock.setLocked(false);
        } else {
            header.setVisibility(View.GONE);
            photoViewAttacher = new PhotoViewAttacher(image);
            photoViewAttacher.setZoomable(true);
            onViewPagerLock.setLocked(true);
            photoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    state = true;
                    checkState(viewLayout);
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        api = Odnoklassniki.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("state")) {
            state = savedInstanceState.getBoolean("state");
        }
        Bundle args = getArguments();
        photo = Photo.get(args.getString("photoId"));
        final View viewLayout = inflater.inflate(R.layout.photoactivity_page, container, false);
        TextView userName = (TextView) viewLayout.findViewById(R.id.photoactivity_page_user);
        TextView albumName = (TextView) viewLayout.findViewById(R.id.photoactivity_page_album);
        Album album = Album.get(photo.album_id);
        if (album.albumType == AlbumType.USER) {
            User user = User.get(album.user_id);
            userName.setText(user.name);
        } else {
            Group group = Group.get(album.group_id);
            userName.setText(group.name);
        }
        albumName.setText(album.title);

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
        likesCount = (TextView) viewLayout.findViewById(R.id.photoactivity_page_likescount);
        likesCount.setText(photo.like_count + "");

        progressBar = (ProgressBar) viewLayout.findViewById(R.id.photoactivity_page_progress);
        progressBar.setVisibility(View.VISIBLE);

        image = (SmartImage) viewLayout.findViewById(R.id.photoactivity_page_image);
        image.setOnSmartViewLoadedListener(this);
        image.setVisibility(View.GONE);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = false;
                checkState(viewLayout);
            }
        });
        checkState(viewLayout);
        image.loadFromURL(photo.getMaxSize().getUrl());
        likeButton = (ImageButton) viewLayout.findViewById(R.id.photoactivity_page_like);
        if (photo.user_id.equals(User.currentUID)) {
            likeButton.setEnabled(false);
        } else {
            likeButton.setOnClickListener(this);
            if (photo.liked_it) {
                likeButton.setImageResource(R.drawable._0001_big_liked);
                likeButton.setEnabled(false);
            }
        }
        ImageButton backButton = (ImageButton) viewLayout.findViewById(R.id.photoactivity_page_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return viewLayout;
    }


    @Override
    public void onSmartViewUpdated() {
        image.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
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
        outState.putBoolean("state", state);
    }

    public interface OnViewPagerLock {
        public void setLocked(boolean isLocked);
    }

    public void setViewPagerLockListener(OnViewPagerLock onViewPagerLock) {
        this.onViewPagerLock = onViewPagerLock;
    }
}

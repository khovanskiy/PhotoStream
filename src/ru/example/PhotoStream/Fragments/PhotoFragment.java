package ru.example.PhotoStream.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.w3c.dom.Text;
import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;

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
                Console.print("Like response: " + responseString);
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
            likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo_view_like_active, 0, 0, 0);
            likeButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            likeButton.setEnabled(false);
            likesCount.setText((photo.like_count + 1) + "");
        }
    }

    protected Odnoklassniki api;
    protected Photo photo;
    protected Button likeButton;
    protected TextView likesCount;
    protected ProgressBar progressBar;
    protected SmartImage image;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        api = Odnoklassniki.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        photo = Photo.get(args.getString("photoId"));

        final View viewLayout = inflater.inflate(R.layout.photoactivity_page, container, false);
        TextView header = (TextView) viewLayout.findViewById(R.id.photoactivity_page_header);
        Album album = Album.get(photo.album_id);
        if (album.albumType == AlbumType.USER) {
            User user = User.get(album.user_id);
            header.setText(user.name + " " + album.title);
        } else {
            Group group = Group.get(album.group_id);
            header.setText(group.name + " " + album.title);
        }

        Date now = new Date();
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

        TextView description = (TextView) viewLayout.findViewById(R.id.photoactivity_page_description);
        description.setText(photo.text);

        likesCount = (TextView) viewLayout.findViewById(R.id.photoactivity_page_likescount);
        likesCount.setText(photo.like_count + "");

        progressBar = (ProgressBar) viewLayout.findViewById(R.id.photoactivity_page_progress);
        image = (SmartImage) viewLayout.findViewById(R.id.photoactivity_page_image);
        image.setOnSmartViewLoadedListener(this);

        image.setVisibility(View.GONE);
        image.setOnClickListener(new View.OnClickListener() {
            private boolean state = false;
            @Override
            public void onClick(View v) {
                LinearLayout header = (LinearLayout) viewLayout.findViewById(R.id.photoactivity_page_full_header);
                LinearLayout footer = (LinearLayout) viewLayout.findViewById(R.id.photoactivity_page_full_footer);
                TextView description = (TextView) viewLayout.findViewById(R.id.photoactivity_page_description);
                if (state) {
                    header.setVisibility(View.VISIBLE);
                    footer.setVisibility(View.VISIBLE);
                    description.setVisibility(View.VISIBLE);
                } else {
                    header.setVisibility(View.GONE);
                    footer.setVisibility(View.GONE);
                    description.setVisibility(View.GONE);
                }
                state = !state;
            }
        });
        progressBar.setVisibility(View.VISIBLE);

        image.loadFromURL(photo.getMaxSize().getUrl());
        likeButton = (Button) viewLayout.findViewById(R.id.photoactivity_page_like);
        if (photo.user_id.equals(User.currentUID)) {
            likeButton.setEnabled(false);
            likeButton.setVisibility(View.GONE);
        } else {
            likeButton.setOnClickListener(this);
            if (photo.liked_it) {
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo_view_like_active, 0, 0, 0);
                likeButton.setBackgroundColor(getResources().getColor(android.R.color.black));
                likeButton.setEnabled(false);
            }
        }


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
}

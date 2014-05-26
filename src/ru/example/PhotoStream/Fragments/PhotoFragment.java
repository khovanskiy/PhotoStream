package ru.example.PhotoStream.Fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import ru.example.PhotoStream.*;
import ru.ok.android.sdk.Odnoklassniki;

import java.util.HashMap;
import java.util.Map;

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
            likeButton.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_photo_view_like_active, 0, 0, 0);
            likeButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            likeButton.setEnabled(false);
        }
    }

    protected Odnoklassniki api;
    protected Photo photo;
    protected Button likeButton;
    protected ProgressBar progressBar;
    protected SmartImage image;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        api = Odnoklassniki.getInstance(getActivity());
        Album album = Album.get(photo.album_id);
        if (album.albumType == AlbumType.USER) {
            User user = User.get(album.user_id);
            getActivity().setTitle(user.name + " " + album.title);
        } else {
            Group group = Group.get(album.group_id);
            getActivity().setTitle(group.name + " " + album.title);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        photo = Photo.get(args.getString("photoId"));
        View viewLayout = inflater.inflate(R.layout.photoactivity_page, container, false);
        progressBar = (ProgressBar) viewLayout.findViewById(R.id.photoactivity_page_progress);
        image = (SmartImage) viewLayout.findViewById(R.id.photoactivity_page_image);
        image.setOnSmartViewLoadedListener(this);

        image.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        image.loadFromURL(photo.pic1024x768);
        likeButton = (Button) viewLayout.findViewById(R.id.photoactivity_page_like);
        likeButton.setOnClickListener(this);
        if (photo.liked_it) {
            likeButton.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_photo_view_like_active, 0, 0, 0);
            likeButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            likeButton.setEnabled(false);
        }
        return viewLayout;
    }


    @Override
    public void onSmartViewLoaded() {
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

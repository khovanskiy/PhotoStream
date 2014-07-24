package ru.example.PhotoStream.ViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import ru.example.PhotoStream.Photo;
import ru.example.PhotoStream.R;
import ru.example.PhotoStream.SmartImage;

import java.util.ArrayList;
import java.util.List;

public class PhotosAdapter extends BaseAdapter {

    private List<Photo> photos = new ArrayList<>();
    private LayoutInflater inflater;
    private boolean isStatic;

    public PhotosAdapter(Context context) {
        this(context, false);
    }

    public PhotosAdapter(Context context, boolean shouldStatic) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isStatic = shouldStatic;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void clear() {
        photos.clear();
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        SmartImage image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Photo photo = photos.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.streamphotoview, parent, false);
            holder = new ViewHolder();
            holder.image = (SmartImage) convertView.findViewById(R.id.streamphotoview_imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            //holder.image.setImageBitmap(null);
        }
        if (isStatic) {
            holder.image.setClickable(false);
        }
        holder.image.loadFromURL(photo.pic180min);
        return convertView;
    }
}

package ru.example.PhotoStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Photo {

    public static class Size {

        private final int width;
        private final int height;
        private final String url;

        public Size(int width, int height, String url) {
            this.width = width;
            this.height = height;
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getUrl() {
            return url;
        }
    }

    private static final Size DEFAULT_SIZE = new Size(0, 0, "");

    private List<Size> sizes = new ArrayList<>(3);

    /**
     * Photo's id.
     */
    public String id = "";

    public String text = "";

    /**
     * Creation time in milliseconds.
     */
    public long created_ms = 0;

    /**
     * Id of the album where the photo is located or null if the album is private.
     */
    public String album_id = "";

    /**
     * Url of 50x50 version.
     */
    public String pic50x50 = "";

    /**
     * Url of 128x128 version.
     */
    public String pic128x128 = "";

    /**
     * Url of 190x190 version.
     */
    public String pic190x190 = "";

    /**
     * Url of 640x480 version.
     */
    public String pic640x480 = "";

    /**
     * Url of 1024x768 version.
     */
    public String pic1024x768 = "";

    /**
     * Comments' count.
     */
    public int comments_count = 0;

    /**
     * Likes' count.
     */
    public int like_count = 0;

    /**
     * Shows if the user has already liked this photo.
     */
    public Boolean liked_it = false;

    /**
     * User id of the photo's owner.
     */
    public String user_id = "";

    private static Map<String, Photo> cache = new ConcurrentHashMap<>();

    private String maxSize = "";

    private Photo() {
    }

    /**
     * Returns photo by its id.
     * @param photoId photo's id
     * @return photo
     */
    public static synchronized Photo get(String photoId) {
        Photo current = null;
        if (!cache.containsKey(photoId)) {
            current = new Photo();
            cache.put(photoId, current);
        } else {
            current = cache.get(photoId);
        }
        current.id = photoId;
        return current;
    }

    /**
     * Builds photo from its JSON representation received from server.
     * @param object JSON form
     * @return photo
     * @throws JSONException
     */

    public static Photo build(JSONObject object) throws JSONException {
        Photo current;
        if (object.has("id")) {
            current = Photo.get(object.getString("id"));
        } else {
            throw new JSONException("User object does not have ID");
        }
        if (object.has("text")) {
            current.text = object.getString("text");
        }
        if (object.has("created_ms")) {
            current.created_ms = object.getLong("created_ms");
        }
        if (object.has("album_id")) {
            current.album_id = object.getString("album_id");
        }
        if (object.has("pic50x50")) {
            current.pic50x50 = object.getString("pic50x50");
            current.sizes.add(new Size(50, 50, current.pic50x50));
        }
        if (object.has("pic128x128")) {
            current.pic128x128 = object.getString("pic128x128");
            current.sizes.add(new Size(128, 128, current.pic128x128));
        }
        if (object.has("pic190x190")) {
            current.pic190x190 = object.getString("pic190x190");
            current.sizes.add(new Size(190, 190, current.pic190x190));
        }
        if (object.has("pic640x480")) {
            current.pic640x480 = object.getString("pic640x480");
            current.sizes.add(new Size(640, 480, current.pic640x480));
        }
        if (object.has("pic1024x768")) {
            current.pic1024x768 = object.getString("pic1024x768");
            current.sizes.add(new Size(1024, 768, current.pic1024x768));
        }
        if (object.has("liked_it")) {
            current.liked_it = object.getBoolean("liked_it");
        }
        if (object.has("like_count")) {
            current.like_count = object.getInt("like_count");
        }
        if (object.has("comments_count")) {
            current.comments_count = object.getInt("comments_count");
        }
        if (object.has("user_id")) {
            current.user_id = object.getString("user_id");
        }
        /*if (object.has("mark_avg")) {
            current.mark_avg = object.getString("mark_avg");    // output: 5+
        }*/
        return current;
    }

    public Size findBestSize(int targetWidth, int targetHeight) {
        final float k = 0.75f;
        for (int i = 0; i < sizes.size(); ++i) {
            if (sizes.get(i).getWidth() >= targetWidth && sizes.get(i).getWidth() >= targetHeight * k ||
                    sizes.get(i).getWidth() >= targetWidth * k && sizes.get(i).getWidth() >= targetHeight) {
                return sizes.get(i);
            }
        }
        return null;
    }

    public Size getMaxSize() {
        if (sizes.size() == 0) {
            return null;
        }
        int j = 0;
        long maxPoints = sizes.get(0).getWidth() * sizes.get(0).getHeight();
        for (int i = 1; i < sizes.size(); ++i) {
            long currentPoints = sizes.get(i).getWidth() * sizes.get(i).getHeight();
            if (currentPoints >= maxPoints) {
                j = i;
                maxPoints = currentPoints;
            }
        }/**/
        return sizes.get(j);
        //return sizes.get(sizes.size() - 1);
    }

    public boolean hasAnySize() {
        return sizes.size() != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Photo) {
            Photo obj = (Photo) o;
            return this.id.equals(obj.id);
        }
        return false;
    }
}

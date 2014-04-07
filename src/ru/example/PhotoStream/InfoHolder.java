package ru.example.PhotoStream;

import org.json.JSONObject;

import java.util.*;

/**
 * Created by Genyaz on 01.04.14.
 */
public class InfoHolder {


    /**
     * Compares photo JSON objects by their upload time.
     */
    public static class PhotoByUploadTimeComparator implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                long l = lhs.getInt("created_ms"), r = rhs.getInt("created_ms");
                if (l > r) {
                    return -1;
                } else if (l > r) {
                    return 1;
                } else {
                    return -new Long(Long.parseLong(lhs.getString("id"))).compareTo(new Long(Long.parseLong(rhs.getString("id"))));
                }
            } catch (Exception e) {
                Console.print(e.getMessage());
            }
            return 0;
        }
    }

    /**
     * Contains current user friends ids (fid).
     */
    public static List<String> friendIds = new ArrayList<String>();

    /**
     * Contains mapping from friend id to friend info.
     */
    public static Map<String, JSONObject> friendInfo = new HashMap<String, JSONObject>();

    /**
     * Contains current user group ids (gid).
     */
    public static List<String> groupIds = new ArrayList<String>();

    /**
     * Contains mapping from group id to group info.
     */
    public static Map<String, JSONObject> groupInfo = new HashMap<String, JSONObject>();

    /**
     * Contains user albums.
     */
    public static List<JSONObject> userAlbums = new ArrayList<JSONObject>();

    /**
     * Contains user private photos sorted by upload time.
     */
    public static SortedSet<JSONObject> userPrivatePhotos =
                new TreeSet<JSONObject>(new PhotoByUploadTimeComparator());

    /**
     * Contains user photos sorted by upload time.
     */
    public static SortedSet<JSONObject> userPhotos =
            new TreeSet<JSONObject>(new PhotoByUploadTimeComparator());
    /**
     * Contains mapping from friend id to friend albums.
     */
    public static Map<String, List<JSONObject>> friendAlbums = new HashMap<String, List<JSONObject>>();

    /**
     * Contains mapping from friend id to all friend photos sorted by upload time.
     */
    public static Map<String, SortedSet<JSONObject>> friendPhotos = new HashMap<String, SortedSet<JSONObject>>();

    /**
     * Contains mapping from group id to group albums.
     */
    public static Map<String, List<JSONObject>> groupAlbums = new HashMap<String, List<JSONObject>>();

    /**
     * Contains mapping from group id to all group photos sorted by time.
     */
    public static Map<String, SortedSet<JSONObject>> groupPhotos = new HashMap<String, SortedSet<JSONObject>>();

    /**
     * Contains mapping from album id (aid) to album info.
     */
    public static Map<String, JSONObject> allAlbums = new HashMap<String, JSONObject>();

    /**
     * Contains mapping from album id to album photos sorted by upload time.
     */
    public static Map<String, SortedSet<JSONObject>> albumPhotos =
            new HashMap<String, SortedSet<JSONObject>>();

    /**
     * Contains mapping from friend id to friend private photos sorted by upload time.
     */
    public static Map<String, SortedSet<JSONObject>> friendPrivatePhotos =
            new HashMap<String, SortedSet<JSONObject>>();

    /**
     * Contains mapping from photo id (photo_id) to photo info.
     */
    public static Map<String, JSONObject> allPhotos = new HashMap<String, JSONObject>();

    /**
     * Contains all photos sorted by upload time.
     */
    public static SortedSet<JSONObject> sortedPhotos =
            new TreeSet<JSONObject>(new PhotoByUploadTimeComparator());

    public static boolean infoDownloaded = false;

    public static void clear() {
        infoDownloaded = false;
        friendIds.clear();
        friendInfo.clear();
        groupIds.clear();
        groupInfo.clear();
        userAlbums.clear();
        userPrivatePhotos.clear();
        userPhotos.clear();
        friendAlbums.clear();
        friendPhotos.clear();
        groupAlbums.clear();
        groupPhotos.clear();
        allAlbums.clear();
        albumPhotos.clear();
        friendPrivatePhotos.clear();
        allPhotos.clear();
        sortedPhotos.clear();
    }
}

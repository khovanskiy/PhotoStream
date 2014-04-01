package ru.example.PhotoStream;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Genyaz on 01.04.14.
 */
public class InfoHolder {
    public static List<String> friendIds = new ArrayList<String>();
    public static List<String> groupIds = new ArrayList<String>();
    public static List<JSONObject> userAlbums = new ArrayList<JSONObject>();
    public static List<JSONObject> userPrivatePhotos = new ArrayList<JSONObject>();
    public static HashMap<String, List<JSONObject>> friendAlbums = new HashMap<String, List<JSONObject>>();
    public static HashMap<String, List<JSONObject>> groupAlbums = new HashMap<String, List<JSONObject>>();
    public static HashMap<String, List<JSONObject>> albumPhotos = new HashMap<String, List<JSONObject>>();
    public static HashMap<String, List<JSONObject>> friendPrivatePhotos = new HashMap<String, List<JSONObject>>();

    public static void clear() {
        friendIds.clear();
        groupIds.clear();
        userAlbums.clear();
        userPrivatePhotos.clear();
        friendAlbums.clear();
        groupAlbums.clear();
        albumPhotos.clear();
        friendPrivatePhotos.clear();
    }
}

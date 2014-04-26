package ru.example.PhotoStream;

public class Group {
    public String uid = "";
    public String name = "";
    public String description = "";
    public String shortname = "";
    public String photo_id = "";
    public boolean shop_visible_admin = false;
    public boolean shop_visible_public = false;
    public int members_count = 0;

    public Photo photo = new Photo();
}

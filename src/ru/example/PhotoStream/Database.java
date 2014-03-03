package ru.example.PhotoStream;

public class Database
{
    private static final String DATABASE_NAME = "photostream_ok";

    private static Database instance = null;

    public Database getInstance()
    {
        if (instance == null)
        {
            instance = new Database();
        }
        return instance;
    }
}

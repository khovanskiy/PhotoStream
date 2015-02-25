package ru.example.PhotoStream;

/**
 * Created by victor on 22.02.2015.
 */
public class OKError {
    /**
     * Request which caused error
     */
    public OKRequest request;
    public String errorCode;

    public OKError(String errorCode) {
        this.errorCode = errorCode;
    }
}

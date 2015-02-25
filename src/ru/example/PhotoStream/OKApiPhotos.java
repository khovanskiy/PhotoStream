package ru.example.PhotoStream;

public class OKApiPhotos extends OKApiBase {
    public OKRequest get() {
        return new OKRequest("") {
            @Override
            protected void start() {
                super.start();
            }
        };
    }
}

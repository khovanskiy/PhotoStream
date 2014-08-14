package ru.example.PhotoStream.Tasks;

import ru.ok.android.sdk.Odnoklassniki;

public interface Task<T> {
    T execute(Odnoklassniki api);
}

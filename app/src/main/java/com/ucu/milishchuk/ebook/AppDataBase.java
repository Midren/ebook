package com.ucu.milishchuk.ebook;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {BookCard.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract BookCardDao bookCardDao();
}

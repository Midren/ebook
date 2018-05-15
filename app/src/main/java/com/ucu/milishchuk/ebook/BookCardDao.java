package com.ucu.milishchuk.ebook;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface BookCardDao {

    @Query("SELECT * FROM bookcard")
    Flowable<List<BookCard> > getAll();

    @Query("SELECT * FROM bookcard WHERE id = :id")
    Flowable<List<BookCard> > getById(long id);

    @Insert
    void insert(BookCard bookCard);

    @Update
    void update(BookCard bookCard);

    @Delete
    void delete(BookCard bookCard);
}

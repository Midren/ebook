package com.ucu.milishchuk.ebook;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BookCardDao {

    @Query("SELECT * FROM bookcard")
    List<BookCard> getAll();

    @Query("SELECT * FROM bookcard WHERE id = :id")
    BookCard getById(long id);

    @Insert
    void insert(BookCard bookCard);

    @Update
    void update(BookCard bookCard);

    @Delete
    void delete(BookCard bookCard);
}

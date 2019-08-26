package com.example.android.noteart.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notas WHERE archivada IS :archivada ORDER BY titulo ASC")
    LiveData<List<NoteEntity>> loadAllNotesTitleASC(int archivada);

    @Query("SELECT * FROM notas WHERE archivada IS :archivada ORDER BY titulo DESC")
    LiveData<List<NoteEntity>> loadAllNotesTitleDESC(int archivada);

    @Query("SELECT * FROM notas WHERE archivada IS :archivada ORDER BY fecha ASC")
    LiveData<List<NoteEntity>> loadAllNotesDateASC(int archivada);

    @Query("SELECT * FROM notas WHERE archivada IS :archivada ORDER BY fecha DESC")
    LiveData<List<NoteEntity>> loadAllNotesDateDESC(int archivada);

    @Insert
    void insertNewNote(NoteEntity note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateNote(NoteEntity note);

    @Query("SELECT * FROM notas WHERE id IS :key")
    LiveData<NoteEntity> loadNote(int key);

    @Delete
    void deleteNote(NoteEntity note);
}

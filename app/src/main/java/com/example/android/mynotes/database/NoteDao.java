package com.example.android.mynotes.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

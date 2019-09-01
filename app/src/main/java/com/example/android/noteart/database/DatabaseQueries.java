package com.example.android.noteart.database;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;

import com.example.android.noteart.adapters.NoteListAdapter;

import java.util.List;

public class DatabaseQueries {

    /**
     *
     * createNoteToArchived: crear nota y guardar lo mismo pero en archivadas o no
     *
     * */
    public static void createNote(NoteEntity note, int archived, int esChecklist, Context ctx) {
        NoteEntity newNote = new NoteEntity(
                note.getTitulo(),
                note.getDescripcion(),
                note.getCheckbox(),
                note.getFecha(),
                archived,
                esChecklist
        );
        DatabaseQueries.insertQuery(newNote, ctx);
    }

    /**
     *
     * deleteQuery: DELETE FROM note
     *
     * */
    public static void deleteQuery(final NoteEntity note, final Context ctx) {
        NoteArtExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                NoteArtDatabase.getsInstance(ctx).noteDao().deleteNote(note);
            }
        });
    }

    /**
     *
     * insertQuery: INSERT noteEntity IN note
     *
     * */
    public static void insertQuery(final NoteEntity note, final Context ctx) {
        NoteArtExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                NoteArtDatabase.getsInstance(ctx).noteDao().insertNewNote(note);
            }
        });
    }

    /**
     *
     * updateQuery: UPDATE note {FIELDS} WHERE id = :id
     *
     * */
    public static void updateQuery(final NoteEntity note, final Context ctx) {
        NoteArtExecutors.getsInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                NoteArtDatabase.getsInstance(ctx)
                        .noteDao()
                        .updateNote(note);
            }
        });
    }

    /**
     *
     * loadNotes: Recoge de la base de datos todas las notas disponibles en base a los
     * filtros y orden del usuario
     *
     * */
    public static void loadNotes(Context ctx, String filter, String order, int archivada,
                          final NoteListAdapter adapter, final LifecycleOwner lf) {
        final String orderAsc = "ASC";
        final String TITULO = "Título";
        final String FECHA = "Fecha";

        if (order.equals(orderAsc)) {
            switch (filter) {
                case TITULO:
                    final LiveData<List<NoteEntity>> notesTitle = NoteArtDatabase.getsInstance(ctx)
                            .noteDao().loadAllNotesTitleASC(archivada);
                    notesTitle.observe(lf, new Observer<List<NoteEntity>>() {
                        @Override
                        public void onChanged(List<NoteEntity> noteEntities) {
                            adapter.setNotes(noteEntities);
                        }
                    });
                    break;
                case FECHA:
                    final LiveData<List<NoteEntity>> notesFecha = NoteArtDatabase.getsInstance(ctx)
                            .noteDao().loadAllNotesDateASC(archivada);
                    notesFecha.observe(lf, new Observer<List<NoteEntity>>() {
                        @Override
                        public void onChanged(List<NoteEntity> noteEntities) {
                            adapter.setNotes(noteEntities);
                        }
                    });
                    break;
            }
        } else {
            switch (filter) {
                case TITULO:
                    final LiveData<List<NoteEntity>> notesTitle = NoteArtDatabase.getsInstance(ctx)
                            .noteDao().loadAllNotesTitleDESC(archivada);
                    notesTitle.observe(lf, new Observer<List<NoteEntity>>() {
                        @Override
                        public void onChanged(List<NoteEntity> noteEntities) {
                            adapter.setNotes(noteEntities);
                        }
                    });
                    break;
                case FECHA:
                    final LiveData<List<NoteEntity>> notesFecha = NoteArtDatabase.getsInstance(ctx)
                            .noteDao().loadAllNotesDateDESC(archivada);
                    notesFecha.observe(lf, new Observer<List<NoteEntity>>() {
                        @Override
                        public void onChanged(List<NoteEntity> noteEntities) {
                            adapter.setNotes(noteEntities);
                        }
                    });
                    break;
            }
        }
    }
}

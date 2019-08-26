package com.example.android.noteart.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {NoteEntity.class}, version = 9, exportSchema = false)
@TypeConverters(TypeConversors.class)
public abstract class NoteArtDatabase extends RoomDatabase {

    private static final String LOG_TAG = NoteArtDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String NAME = "noteart";
    private static NoteArtDatabase sInstance;

    private static final Migration MIGRATION2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'nota' ADD COLUMN 'archivada' INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE 'checklist' ('id' INTEGER NOT NULL PRIMARY KEY, 'titulo' TEXT, " +
                    "'lista_elementos' TEXT, 'lista_elementos_checkbox' TEXT, 'prioridad' INTEGER NOT NULL," +
                    "'fecha' INTEGER)");
        }
    };

    private static final Migration MIGRATION5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'nota' ADD COLUMN 'checkbox' TEXT NOT NULL DEFAULT 'false'");
        }
    };

    private static final Migration MIGRATION6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'nota' ADD COLUMN 'checklist' INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION7_8 = new Migration(5, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE 'nota'");
            database.execSQL("CREATE TABLE 'notas' ('id' INTEGER NOT NULL PRIMARY KEY, 'titulo' TEXT," +
                             "'descripcion' TEXT, 'checkbox' TEXT, 'prioridad' INTEGER NOT NULL," +
                             "'fecha' INTEGER, 'archivada' INTEGER NOT NULL)");
        }
    };

    private static final Migration MIGRATION8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE 'notas'");
            database.execSQL("CREATE TABLE 'notas' ('id' INTEGER NOT NULL PRIMARY KEY, 'titulo' TEXT," +
                    "'descripcion' TEXT, 'checkbox' TEXT, 'esChecklist' INTEGER NOT NULL," +
                    "'fecha' INTEGER, 'archivada' INTEGER NOT NULL)");
        }
    };

    public static NoteArtDatabase getsInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(
                        ctx.getApplicationContext(),
                        NoteArtDatabase.class,
                        NAME)
                        .addMigrations(MIGRATION8_9)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract NoteDao noteDao();
}

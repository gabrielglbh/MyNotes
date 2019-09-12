package com.example.android.mynotes.database;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NoteArtExecutors {

    private static final Object LOCK = new Object();
    private static NoteArtExecutors sInstance;
    private final Executor diskIO;

    private NoteArtExecutors(Executor exec) { diskIO = exec; }

    public static NoteArtExecutors getsInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new NoteArtExecutors(Executors.newSingleThreadExecutor());
            }
        }
        return sInstance;
    }

    public Executor getDiskIO() { return diskIO; }

    private static class mte implements Executor {
        private Handler handler = new Handler(Looper.getMainLooper());

        public void execute(Runnable runnable) { handler.post(runnable); }
    }

}

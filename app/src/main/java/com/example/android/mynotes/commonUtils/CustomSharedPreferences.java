package com.example.android.mynotes.commonUtils;

import android.content.Context;
import android.preference.PreferenceManager;

public class CustomSharedPreferences {

    /**
     *
     * setSharedPreferences: filter and order entered by the user are saved
     *
     * */
    public static void setSharedPreferences(Context ctx, String[] id, String[] data) {
        android.content.SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        android.content.SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < id.length; i++) {
            editor.putString(id[i], data[i]);
        }
        editor.apply();
    }

    /**
     *
     * setSharedPreferencesDeleteMode: mode (delete or not) on List of Notes is saved for use on SwipeHandleToArchived
     *
     * */
    public static void setSharedPreferencesDeleteMode(Context ctx, String id, boolean deleteMode) {
        android.content.SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        android.content.SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(id, deleteMode);
        editor.apply();
    }

}

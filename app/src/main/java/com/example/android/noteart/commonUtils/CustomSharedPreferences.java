package com.example.android.noteart.commonUtils;

import android.content.Context;
import android.preference.PreferenceManager;

public class CustomSharedPreferences {

    /**
     *
     * setSharedPreferences: se escriben el filtro y el orden en onStop en sharedPreferences
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
     * setSharedPreferencesDeleteMode: se escribe el modo seleccionado en sharedPreferences para
     * su uso en SwipeHandle
     *
     * */
    public static void setSharedPreferencesDeleteMode(Context ctx, String id, boolean deleteMode) {
        android.content.SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        android.content.SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(id, deleteMode);
        editor.apply();
    }

}

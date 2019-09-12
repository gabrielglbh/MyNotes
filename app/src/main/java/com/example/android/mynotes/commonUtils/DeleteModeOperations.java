package com.example.android.mynotes.commonUtils;

import android.content.Context;
import android.view.Menu;
import android.widget.LinearLayout;

import com.example.android.mynotes.R;
import com.example.android.mynotes.database.NoteEntity;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

public class DeleteModeOperations {

    /**
     *
     * changeFrameColorList: changes the color of linear layouts to colorPrimaryActionBar
     *
     * */
    private static void changeFrameColorList(ArrayList<LinearLayout> tv, Context ctx) {
        for (int x = 0; x < tv.size(); x++) {
            changeColorFrame(tv.get(x), ctx, R.color.colorPrimaryActionBar);
        }
    }

    /**
     *
     * changeColorFrame: chenges color of individual linear layour to 'color'
     *
     * */
    public static void changeColorFrame(LinearLayout tv, Context ctx, int color) {
        tv.setBackgroundColor(ContextCompat.getColor(ctx, color));
    }

    public static void removeNote(int id, LinearLayout frame, NoteEntity note,
                                  ArrayList<Integer> idList,
                                  ArrayList<LinearLayout> tvList,
                                  ArrayList<NoteEntity> noteList) {
        idList.remove(idList.indexOf(id));
        tvList.remove(frame);
        noteList.remove(note);
    }

    /**
     *
     * deleteModeShutdownNotes: manages the exit of delete mode
     *
     **/
    public static boolean deleteModeShutdownNotes(Context ctx, ArrayList<LinearLayout> tvList, Menu menu,
                                                  ArrayList<NoteEntity> noteList, boolean isArchived) {
        DeleteModeOperations.changeFrameColorList(tvList, ctx);
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(true);
        menu.getItem(3).setVisible(true);
        if(!isArchived) {
            menu.getItem(4).setVisible(true);
        }
        noteList.clear();
        tvList.clear();
        return false;
    }
}

package com.example.android.mynotes.commonUtils;

import android.content.Context;
import android.view.Menu;
import android.widget.LinearLayout;

import com.example.android.mynotes.R;
import com.example.android.mynotes.database.NoteEntity;

import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class DeleteModeOperations {

    /**
     *
     * changeFrameColorList: changes the color of linear layouts to colorPrimaryActionBar
     *
     * */
    private static void changeFrameColorList(ArrayList<CardView> tv, ArrayList<LinearLayout> ll, Context ctx) {
        for (int x = 0; x < tv.size(); x++) {
            changeColorFrame(tv.get(x), ll.get(x), ctx, R.color.colorPrimaryActionBar);
        }
    }

    /**
     *
     * changeColorFrame: chenges color of individual linear layour to 'color'
     *
     * */
    public static void changeColorFrame(CardView tv, LinearLayout ll, Context ctx, int color) {
        tv.setCardBackgroundColor(ContextCompat.getColor(ctx, color));
        ll.setBackgroundColor(ContextCompat.getColor(ctx, color));
    }

    public static void removeNote(int id, CardView frame, NoteEntity note,
                                  ArrayList<Integer> idList,
                                  ArrayList<CardView> tvList,
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
    public static boolean deleteModeShutdownNotes(Context ctx, ArrayList<CardView> tvList,
                                                  ArrayList<LinearLayout> llList, Menu menu,
                                                  ArrayList<NoteEntity> noteList, boolean isArchived) {
        DeleteModeOperations.changeFrameColorList(tvList, llList, ctx);
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

package com.example.android.noteart.commonUtils;

import android.content.Context;
import android.view.Menu;
import android.widget.LinearLayout;

import com.example.android.noteart.R;
import com.example.android.noteart.database.NoteEntity;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

public class DeleteModeOperations {

    /**
     *
     * changeFrameColorList: Recorre tv y cambia el color del frame a 'no seleccionado'
     *
     * */
    private static void changeFrameColorList(ArrayList<LinearLayout> tv, Context ctx) {
        for (int x = 0; x < tv.size(); x++) {
            changeColorFrame(tv.get(x), ctx, R.color.colorPrimaryActionBar);
        }
    }

    /**
     *
     * changeColorFrame: Cambia el color del tv a color
     *
     * */
    public static void changeColorFrame(LinearLayout tv, Context ctx, int color) {
        tv.setBackgroundColor(ContextCompat.getColor(ctx, color));
    }

    /**
     *
     * removeNote: Elimina una nota previamente seleccionada en DeleteMode
     *
     * */
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
     * deleteModeShutdownNotes: Se maneja el cierre del modo selección
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
            menu.getItem(5).setVisible(true);
        }
        noteList.clear();
        tvList.clear();
        return false;
    }
}

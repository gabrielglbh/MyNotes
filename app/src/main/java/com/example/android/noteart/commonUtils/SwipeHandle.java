package com.example.android.noteart.commonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.example.android.noteart.adapters.NoteListAdapter;
import com.example.android.noteart.R;
import com.example.android.noteart.database.NoteEntity;

import java.util.List;

/**
 *
 * handleMovementTouch: Manejo y administración de los clikcs y drags de los usuarios
 *
 * */
public class SwipeHandle extends ItemTouchHelper.SimpleCallback{

    private static Toast mToast;
    private NoteListAdapter adapter;
    private Context ctx;
    private int archived;
    private String msg;
    private Drawable icon;
    private ColorDrawable background;

    private String ID_DELETEMODE_BUNDLE = "onDeleteMode";

    public SwipeHandle (NoteListAdapter adapter, Context ctx, int archived, String msg, boolean isArchived) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.ctx = ctx;
        this.archived = archived;
        this.msg = msg;
        if (isArchived) icon = ContextCompat.getDrawable(ctx, R.drawable.ic_unarchive_back);
        else icon = ContextCompat.getDrawable(ctx, R.drawable.ic_archive_back);
        background = new ColorDrawable(ContextCompat.getColor(ctx, R.color.colorAccentBackgroundSwipe));
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    /**
     *
     * onSwiped: Recoge del bundle el valor de isOnDeleteMode
     *
     *      -> Si es true: no permite el swipe y se notifica al adapter para devolver el viewHolder
     *      a su posición inicial
     *
     *      -> Si es false: Hace la operación de restaurar/archivar nota
     *
     * */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean isDeleteModeOn = preferences.getBoolean(ID_DELETEMODE_BUNDLE, true);
        int esChecklist = 0;

        if (isDeleteModeOn) {
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
            makeToast("Operación no permitada en el modo selección", ctx);
        } else {
            int position = viewHolder.getAdapterPosition();
            final List<NoteEntity> notes = adapter.getNotes();
            final NoteEntity note = notes.get(position);

            esChecklist = note.getEsChecklist();
            DatabaseQueries.deleteQuery(note, ctx);
            DatabaseQueries.createNote(note, archived, esChecklist, ctx);
            makeToast(msg, ctx);
        }
    }

    @Override
    public boolean isLongPressDragEnabled() { return true; }

    /**
     *
     * Para dibujar el background onSwipe
     *
     * */
    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;
        int margin = 22;

        int height = itemView.getHeight();
        int right = itemView.getRight();
        int left = itemView.getLeft();
        int top = itemView.getTop() + margin;
        int bottom = itemView.getBottom() - margin;

        int iconMargin = (height - icon.getIntrinsicHeight()) / 2;
        int iconTop = top + (height - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = left + iconMargin;
            int iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(left, top, left + ((int) dX) + backgroundCornerOffset, bottom);

        } else if (dX < 0) { // Swiping to the left
            int iconLeft = right - iconMargin - icon.getIntrinsicWidth();
            int iconRight = right - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(right + ((int) dX) - backgroundCornerOffset, top, right, bottom);

        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        icon.draw(c);
    }

    private static void makeToast(String msg, Context ctx) {
        if (mToast != null) mToast.cancel();
        mToast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

}

package com.example.android.mynotes.commonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.mynotes.adapters.NoteListAdapter;
import com.example.android.mynotes.R;
import com.example.android.mynotes.database.DatabaseQueries;
import com.example.android.mynotes.database.NoteEntity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeHandleToArchived extends ItemTouchHelper.SimpleCallback{

    private static Toast mToast;
    private NoteListAdapter adapter;
    private Context ctx;
    private int archived;
    private String msg;
    private Drawable icon;
    private ColorDrawable background;

    private String ID_DELETEMODE_BUNDLE = "onDeleteMode";

    public SwipeHandleToArchived(NoteListAdapter adapter, Context ctx, int archived, String msg, boolean isArchived) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
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

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean isDeleteModeOn = preferences.getBoolean(ID_DELETEMODE_BUNDLE, true);
        int esChecklist, recordatorio;

        if (isDeleteModeOn) {
            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
            makeToast("Operación no permitada en el modo selección", ctx);
        } else {
            int position = viewHolder.getAdapterPosition();
            final List<NoteEntity> notes = adapter.getNotes();
            final NoteEntity note = notes.get(position);

            esChecklist = note.getEsChecklist();
            recordatorio = note.getRecordatorio();
            DatabaseQueries.deleteQuery(note, ctx);
            DatabaseQueries.createNote(note, archived, esChecklist, recordatorio, ctx);
            makeToast(msg, ctx);
        }
    }

    @Override
    public boolean isLongPressDragEnabled() { return true; }

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

        int iconIntrHeight = icon.getIntrinsicHeight();
        int iconIntrWidth = icon.getIntrinsicWidth();

        int iconMargin = (height - iconIntrHeight) / 2;
        int iconTop = top + iconMargin;
        int iconBottom = iconTop + iconIntrHeight;

        if (dX > 0) { // Swiping to the right
            int iconLeft = left + iconMargin;
            int iconRight = iconLeft + iconIntrWidth;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(left, top, left + ((int) dX) + backgroundCornerOffset, bottom);

        } else if (dX < 0) { // Swiping to the left
            int iconLeft = right - iconMargin - iconIntrWidth;
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

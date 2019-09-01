package com.example.android.noteart.commonUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.android.noteart.R;
import com.example.android.noteart.adapters.CreateCheckListAdapter;

public class SwipeDragAndDropChecklist extends ItemTouchHelper.Callback {

    CreateCheckListAdapter adapter;
    Context ctx;

    public SwipeDragAndDropChecklist(Context ctx, CreateCheckListAdapter adapter) {
        this.ctx = ctx;
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder init,
                          @NonNull RecyclerView.ViewHolder target) {
        if (adapter.isDragHelperTouched) {
            adapter.onItemMove(init.getAdapterPosition(),
                    target.getAdapterPosition());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return adapter.isDragHelperTouched;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        View view = viewHolder.itemView;
        if (isCurrentlyActive && adapter.isDragHelperTouched) {
            view.setBackgroundColor(ContextCompat.getColor(ctx, R.color.selectedCheck));
        } else {
            adapter.isDragHelperTouched = false;
            view.setBackgroundColor(ContextCompat.getColor(ctx, R.color.colorPrimaryActionBar));
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    // Implementado en ChecklistAdapter
    public interface ItemTouchHelperListener {
        boolean onItemMove(int fromPosition, int toPosition);
    }
}

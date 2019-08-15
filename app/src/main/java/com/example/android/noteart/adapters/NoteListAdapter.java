package com.example.android.noteart.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.noteart.R;
import com.example.android.noteart.database.NoteEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    private ListItemClickListener onClicked;
    private Context mContext;
    private List<NoteEntity> mNoteEntityList;

    private static final String DATE_FORMAT = "dd/MM/yy";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public NoteListAdapter(Context ctx, ListItemClickListener listener) {
        mContext = ctx;
        onClicked = listener;
    }

    /********************************************************************************************
    *                                                                                           *
    *                                    METODOS DEL ADAPTER                                    *
    *                                                                                           *
    *********************************************************************************************/
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View element = LayoutInflater.from(mContext)
                .inflate(R.layout.note_element, viewGroup, false);
        return new NoteViewHolder(element);
    }

    /**
     *
     * onBindViewHolder: Se llama cuando se hace el notifyDataSetChanged() en setNotes()
     * Se hace get de la lista
     *
     * */
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i) {
        NoteEntity note = mNoteEntityList.get(i);
        noteViewHolder.mTextViewTitle.setText(note.getTitulo());
        String textOfCheckBox = note.getDescripcion();
        String isCheckedText = note.getCheckbox();
        if (isCheckedText.contains("-")) { // Es una checklist
            String[] textsArr = textOfCheckBox.split("-");
            String[] checksArr = isCheckedText.split("-");
            ArrayList<String> texts = new ArrayList<>(Arrays.asList(textsArr));
            ArrayList<String> checks = new ArrayList<>(Arrays.asList(checksArr));

            String delimiterFalse = "\n\u25A1 ";
            String initFalse = "\u25A1 ";
            String delimiterTrue = "\n\u25A0 ";
            String initTrue = "\u25A0 ";

            if (checksArr.length > textsArr.length) {
                int diff = checksArr.length - textsArr.length;
                for (int x = 0; x < diff; x++) texts.add("");
            }

            for (int x = 0; x < checks.size(); x++) {
                if (x == 0) {
                    if(checks.get(0).equals("true")) texts.set(0,initTrue.concat(texts.get(0)));
                    else texts.set(0, initFalse.concat(texts.get(0)));
                } else {
                    if(checks.get(x).equals("true")) texts.set(x, delimiterTrue.concat(texts.get(x)));
                    else texts.set(x, delimiterFalse.concat(texts.get(x)));
                }
            }

            String finalText = TextUtils.join("", texts);
            noteViewHolder.mTextViewContent.setText(finalText);
        } else { // Es una nota
            noteViewHolder.mTextViewContent.setText(note.getDescripcion());
        }
        noteViewHolder.mTextViewDate.setText(dateFormat.format(note.getFecha()));
        setPriority(note.getPrioridad(), noteViewHolder);
    }

    /**
     *
     * setPriority: hace set del color de la prioridad en base a su id en una nota designada
     *
     * */
    private void setPriority(int id, NoteViewHolder note) {
        switch (id) {
            case 1:
                note.mTextViewPriority.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.priority1),
                        PorterDuff.Mode.SRC_ATOP
                );
                break;
            case 2:
                note.mTextViewPriority.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.priority2),
                        PorterDuff.Mode.SRC_ATOP
                );
                break;
            case 3:
                note.mTextViewPriority.getBackground().setColorFilter(
                        ContextCompat.getColor(mContext, R.color.priority3),
                        PorterDuff.Mode.SRC_ATOP
                );
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mNoteEntityList == null) {
            return 0;
        }
        return mNoteEntityList.size();
    }

    /**
     *
     * getNotes: se recogen todas las notas en el ROOM y en el RecyclerView
     *
     * */
    public List<NoteEntity> getNotes() {
        return mNoteEntityList;
    }

    /**
     *
     * setNotes: Actualiza el RecyclerView con una lueva lista de notas "notes"
     * y lo notifica al adapter con "notifyDataSetChanged()"
     *
     * */
    public void setNotes(List<NoteEntity> notes) {
        mNoteEntityList = notes;
        notifyDataSetChanged();
    }

    /********************************************************************************************
     *                                                                                           *
     *                                METODOS DEL VIEW HOLDER                                    *
     *                                                                                           *
     *********************************************************************************************/

    public interface ListItemClickListener {
        void onElementClicked(int id, NoteEntity note, LinearLayout frame);
        void onElementLongClicked(int id, NoteEntity note, LinearLayout frame);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {

        TextView mTextViewTitle, mTextViewContent, mTextViewDate, mTextViewPriority, mTextViewFrame;
        LinearLayout mLinearLayout;

        NoteViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mTextViewTitle = itemView.findViewById(R.id.tv_title_note);
            mTextViewContent = itemView.findViewById(R.id.tv_content_note);
            mTextViewDate = itemView.findViewById(R.id.tv_date_note);
            mTextViewPriority = itemView.findViewById(R.id.tv_priority_note);
            mTextViewFrame = itemView.findViewById(R.id.tv_frame);
            mLinearLayout = itemView.findViewById(R.id.ll_linear_layout_main);
        }

        @Override
        public void onClick(View view) {
            int id = mNoteEntityList.get(getAdapterPosition()).getId();
            final NoteEntity note = mNoteEntityList.get(getAdapterPosition());
            onClicked.onElementClicked(id, note, mLinearLayout);
        }

        @Override
        public boolean onLongClick(final View view) {
            int id = mNoteEntityList.get(getAdapterPosition()).getId();
            final NoteEntity note = mNoteEntityList.get(getAdapterPosition());
            onClicked.onElementLongClicked(id, note, mLinearLayout);
            return true;
        }
    }
}

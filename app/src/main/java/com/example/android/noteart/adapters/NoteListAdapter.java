package com.example.android.noteart.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.noteart.CreateNoteActivity;
import com.example.android.noteart.R;
import com.example.android.noteart.database.NoteEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    private ListItemClickListener onClicked;
    private Context mContext;
    private List<NoteEntity> mNoteEntityList;

    private static final String DATE_FORMAT = "dd/M/yy";
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

        if (note.getTitulo().isEmpty()) {
            noteViewHolder.mTextViewTitle.setVisibility(View.GONE);
            noteViewHolder.mTextViewBorder.setVisibility(View.GONE);
        }
        else {
            noteViewHolder.mTextViewTitle.setVisibility(View.VISIBLE);
            noteViewHolder.mTextViewBorder.setVisibility(View.VISIBLE);
        }

        if (note.getRecordatorio() == 0){  // No hay recordatorio
            noteViewHolder.mTextViewIcon.setVisibility(View.GONE);
        } else if (note.getRecordatorio() == 1) {  // Hay recordatorio
            String text = "Recordatorio: " + note.getFecha_recordatorio() + " a las " + note.getHora_recordatorio();
            noteViewHolder.mTextViewFecha.setText(text);
            noteViewHolder.mTextViewIcon.setVisibility(View.VISIBLE);
        }

        if (note.getEsChecklist() == 0) { // Es una checklist
            String[] textsArr = textOfCheckBox.split(CreateNoteActivity.DELIMITER);
            String[] checksArr = isCheckedText.split(CreateNoteActivity.DELIMITER);
            ArrayList<String> texts = new ArrayList<>(Arrays.asList(textsArr));
            ArrayList<String> checks = new ArrayList<>(Arrays.asList(checksArr));

            String delimiterFalse = "\n\u25A1 ";
            String initFalse = "\u25A1 ";
            String delimiterTrue = "\n\u25A0 ";
            String initTrue = "\u25A0 ";

            for (int x = 0; x < texts.size(); x++) {
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

        TextView mTextViewTitle, mTextViewContent, mTextViewDate, mTextViewFrame, mTextViewBorder, mTextViewFecha;
        LinearLayout mLinearLayout, mTextViewIcon;

        NoteViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mTextViewTitle = itemView.findViewById(R.id.tv_title_note);
            mTextViewContent = itemView.findViewById(R.id.tv_content_note);
            mTextViewDate = itemView.findViewById(R.id.tv_date_note);
            mTextViewFrame = itemView.findViewById(R.id.tv_frame);
            mLinearLayout = itemView.findViewById(R.id.ll_linear_layout_main);
            mTextViewBorder = itemView.findViewById(R.id.tv_borderline_note);
            mTextViewIcon = itemView.findViewById(R.id.ic_recordatorio_nota);
            mTextViewFecha = itemView.findViewById(R.id.tv_recordatorio_nota);
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

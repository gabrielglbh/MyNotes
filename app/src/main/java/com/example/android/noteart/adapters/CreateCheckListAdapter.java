package com.example.android.noteart.adapters;

import android.content.Context;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.noteart.R;
import com.example.android.noteart.commonUtils.SwipeDragAndDropChecklist.ItemTouchHelperListener;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class CreateCheckListAdapter extends RecyclerView.Adapter<CreateCheckListAdapter.CheckElemViewHolder>
        implements ItemTouchHelperListener {

    private Context mContext;
    private ArrayList<String> textList;
    private ArrayList<Boolean> checkedList;
    private boolean mode;
    public boolean isDragHelperTouched = false;

    public CreateCheckListAdapter(Context ctx, ArrayList<String> editTexts, ArrayList<Boolean> checkedList, boolean mode) {
        mContext = ctx;
        textList = editTexts;
        this.checkedList = checkedList;
        this.mode = mode;
    }

    @NonNull
    @Override
    public CheckElemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View element = LayoutInflater.from(mContext)
                .inflate(R.layout.checklist_element, viewGroup, false);
        return new CreateCheckListAdapter.CheckElemViewHolder(element);
    }

    /**
     *
     * Se tienen dos listas principalmente:
     *      textList para los Strings de cada checkBox
     *      checkedList para los booleanos de los checkbox
     *
     *      Se hacen varios listeners para crear dinámicamente la lista
     *
     * */
    @Override
    public void onBindViewHolder(@NonNull final CheckElemViewHolder checkElemViewHolder, final int pos) {
        final EditText edit = checkElemViewHolder.editText;
        final TextView button = checkElemViewHolder.buttonDelete;
        final int position = checkElemViewHolder.getAdapterPosition();
        final CheckBox checkBox = checkElemViewHolder.checkBox;
        final TextView holder = checkElemViewHolder.dragHelper;

        if (checkedList.get(position)) { setCheckTrue(button, edit); }
        else { setCheckFalse(button, edit); }

        checkBox.setChecked(checkedList.get(position));
        edit.setText(textList.get(position));
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        /**
         * Handler del drag and drop
         **/
        holder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (textList.size() > 1) {
                    isDragHelperTouched = true;
                    return true;
                } else {
                    return false;
                }
            }
        });

        /**
         * Manejo del focus
         * */
        if (textList.contains("") && !mode) {
            if (position == textList.indexOf("")) { edit.requestFocus(); }
        }

        /**
         * Cambio de modo para que al editar, los edit text sigan teniendo focus
         * */
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mode = false;
                } else {
                    final String text = edit.getText().toString();
                    setContent(position, text, checkBox);
                }
            }
        });

        /**
         * Se guarda el texto del edit text y se inserta un nuevo elemento cuando se pulsa el botón
         * */
        edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                final String text = edit.getText().toString();

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (position == textList.size() - 1 || !textList.get(position + 1).trim().isEmpty()) {
                        setContent(position, text, checkBox);
                    } else {
                        setContent(position, text, checkBox);
                        if (textList.size() != 1) { removeElement(position + 1); }
                    }

                    textList.add(position + 1, "");
                    checkedList.add(position + 1, false);
                    notifyItemInserted(position + 1);
                    notifyItemRangeChanged(position, textList.size() - position);
                    return true;
                }
                return false;
            }
        });

        /**
         * Eliminación de un elemento de la checklist
         * */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textList.size() != 1) { removeElement(position); }
            }
        });

        /**
         * Manejo de los checks
         * */
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    if (b) {
                        setCheckTrue(button, edit);
                    } else {
                        setCheckFalse(button, edit);
                    }
                    checkedList.set(position, b);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (textList == null) return 0;
        else return textList.size();
    }

    private void setContent(int position, String text, CheckBox checkBox) {
        if (position <= textList.size() - 1) {
            textList.set(position, text);
            checkedList.set(position, checkBox.isChecked());
        }
    }

    public void addNewElementOnButton() {
        textList.add(textList.size(), "");
        checkedList.add(checkedList.size(), false);
        notifyItemInserted(textList.size() + 1);
        notifyItemRangeChanged(0, textList.size());
    }

    private void removeElement(int position) {
        textList.remove(position);
        checkedList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, textList.size() - (position));
    }

    public void setBinds(ArrayList<String> editTextList, ArrayList<Boolean> isCheckedList) {
        this.textList = editTextList;
        checkedList = isCheckedList;
        notifyDataSetChanged();
    }

    public ArrayList<String> getTextList() {
        return textList;
    }

    public ArrayList<Boolean> getCheckedList() {
        return checkedList;
    }

    private void setCheckTrue(TextView button, EditText edit) {
        button.setVisibility(View.GONE);
        edit.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorPrimaryActionBar));
        edit.setEnabled(false);
        edit.setTextColor(ContextCompat.getColor(mContext, R.color.colorBorderNote));
    }

    private void setCheckFalse(TextView button, EditText edit) {
        button.setVisibility(View.VISIBLE);
        edit.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorTextTitle));
        edit.setEnabled(true);
        edit.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextContent));
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(textList, fromPosition, toPosition);
        Collections.swap(checkedList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    class CheckElemViewHolder extends RecyclerView.ViewHolder {

        EditText editText;
        CheckBox checkBox;
        TextView buttonDelete, dragHelper;

        CheckElemViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.edit_text_checkbox_rv);
            checkBox = itemView.findViewById(R.id.cb_check_box_rv);
            buttonDelete = itemView.findViewById(R.id.button_delete_check);
            dragHelper = itemView.findViewById(R.id.tv_drag_and_move_checklist_elem);
        }
    }

}

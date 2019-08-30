package com.example.android.noteart.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.util.Log;
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

import java.util.ArrayList;

public class CreateCheckListAdapter extends RecyclerView.Adapter<CreateCheckListAdapter.CheckElemViewHolder> {

    private Context mContext;
    private ArrayList<String> textList;
    private ArrayList<Boolean> checkedList;
    private boolean mode;

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

        if (checkedList.get(position)) { setCheckTrue(button, edit); }
        else { setCheckFalse(button, edit); }

        checkBox.setChecked(checkedList.get(position));
        edit.setText(textList.get(position));
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Manejo de aparicion de los botones del elemento
        if (!edit.hasFocus()) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }

        // Manejo del focus
        if (textList.contains("") && !mode) {
            if (position == textList.indexOf("")) edit.requestFocus();
        }

        // Cambio de modo para que al editar, los edit text sigan teniendo focus
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mode = false;
                    button.setVisibility(View.VISIBLE);
                } else {
                    button.setVisibility(View.GONE);
                    final String text = edit.getText().toString();
                    if (position <= textList.size() - 1) {
                        textList.set(position, text);
                        checkedList.set(position, checkBox.isChecked());
                    }
                }
            }
        });

        // Se guarda el texto del edit text y se inserta un nuevo elemento cuando se pulsa el botón
        edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    final String text = edit.getText().toString();
                    if (position <= textList.size() - 1) {
                        textList.set(position, text);
                        checkedList.set(position, checkBox.isChecked());
                        button.setVisibility(View.GONE);
                    }

                    textList.add(position + 1, "");
                    checkedList.add(position + 1, false);
                    notifyItemInserted(position + 1);
                    notifyItemRangeChanged(0, textList.size());
                    return true;
                }
                return false;
            }
        });

        // Eliminación de un elemento de la checklist
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textList.size() != 1) {
                    textList.remove(position);
                    checkedList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, textList.size() - position);
                }
            }
        });

        // Manejo de las checks
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

    public void addNewElementOnButton() {
        textList.add(textList.size(), "");
        checkedList.add(checkedList.size(), false);
        notifyItemInserted(textList.size() + 1);
        notifyItemRangeChanged(0, textList.size());
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

    class CheckElemViewHolder extends RecyclerView.ViewHolder {

        EditText editText;
        CheckBox checkBox;
        TextView buttonDelete;

        CheckElemViewHolder(View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.edit_text_checkbox_rv);
            checkBox = itemView.findViewById(R.id.cb_check_box_rv);
            buttonDelete = itemView.findViewById(R.id.button_delete_check);
        }
    }

}

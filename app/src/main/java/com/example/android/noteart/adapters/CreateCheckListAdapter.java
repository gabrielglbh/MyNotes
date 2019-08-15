package com.example.android.noteart.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

    public CreateCheckListAdapter(Context ctx, ArrayList<String> editTexts, ArrayList<Boolean> checkedList) {
        mContext = ctx;
        textList = editTexts;
        this.checkedList = checkedList;
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
    public void onBindViewHolder(@NonNull final CheckElemViewHolder checkElemViewHolder, int pos) {
        final EditText edit = checkElemViewHolder.editText;
        final TextView button = checkElemViewHolder.buttonDelete;
        final int position = checkElemViewHolder.getAdapterPosition();
        final CheckBox checkBox = checkElemViewHolder.checkBox;

        if (checkedList.get(position)) {
            setCheckTrue(button, edit);
        } else {
            setCheckFalse(button, edit);
        }

        checkBox.setChecked(checkedList.get(position));
        edit.setText(textList.get(position));
        edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edit.requestFocus();

        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                if (count > 0) {
                    final String text = edit.getText().toString();
                    textList.set(position, text);
                    checkedList.set(position, checkBox.isChecked());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    textList.add("");
                    checkedList.add(false);
                    notifyItemInserted(position+1);
                    return true;
                }
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textList.size() > position) {
                    edit.setText("");
                    textList.remove(position);
                    checkedList.remove(position);
                    notifyItemRemoved(position);
                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkedList.set(position, b);
                    setCheckTrue(button, edit);
                }
                else {
                    checkedList.set(position, b);
                    setCheckFalse(button, edit);
                }
            }
        });
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

    @Override
    public int getItemCount() {
        if (textList == null) return 0;
        else return textList.size();
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

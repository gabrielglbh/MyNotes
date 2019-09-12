package com.example.android.mynotes;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.mynotes.adapters.CreateCheckListAdapter;
import com.example.android.mynotes.commonUtils.LinksManagement;
import com.example.android.mynotes.commonUtils.SwipeDragAndDropChecklist;
import com.example.android.mynotes.database.NoteArtDatabase;
import com.example.android.mynotes.database.NoteEntity;
import com.example.android.mynotes.database.DatabaseQueries;
import com.example.android.mynotes.workerUtils.MyWorkerNotifier;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText mEditTextTitle, mEditTextDescription;
    private TextView mTextViewTimePicker, mTextViewDatePicker;
    private TextView mToolbarCreationMode, mTextViewAddElem, mTextViewRecordatorio;
    private LinearLayout ll, mLinearLayoutRecordatorio;
    private ScrollView sc;
    private Toolbar tb;
    private NoteEntity mNote;
    private Toast mToast;
    private Menu mMenu;
    private RecyclerView mRecyclerView;
    private CreateCheckListAdapter mAdapter;
    private ItemTouchHelper touchHelper;
    private AlertDialog alarmBuilder;
    private Button mButtonCancelRecordatorio, mButtonAñadirRecordatorio;

    public static final String NOTE_ID = "id_nota";
    public static final String UPDATE_NOTE = "update_nota";
    public static final String ID_BUNDLE_ARCHIVED = "archivada";
    public static final String ID_CREATION_MODE = "creationMode";
    public static final String CREATION_MODE_1 = "nota";
    public static final String CREATION_MODE_2 = "checklist";
    public static final String DELIMITER = "#/@/#--";

    private boolean archivedPressed = false;
    private boolean isOnDelete = false;
    private String titleOnDelete, descriptionOnDelete, checksOnDelete;
    private String isOnCreationMode, givenDateString, textTimeOnNote, textDateOnNote;
    private String TAG;
    private int esChecklist, tieneRecordatorio;

    private ArrayList<String> editTextListCheckBox = new ArrayList<>();
    private ArrayList<Boolean> isCheckedListCheckBox = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        mEditTextTitle = findViewById(R.id.et_edit_note_title);
        mEditTextDescription = findViewById(R.id.et_edit_note_description);
        mRecyclerView = findViewById(R.id.rv_main_checklist);
        mToolbarCreationMode = findViewById(R.id.toolbar_creation_mode);
        mTextViewAddElem = findViewById(R.id.toolbar_add_elem);
        mTextViewRecordatorio = findViewById(R.id.tv_recordatorio);
        mLinearLayoutRecordatorio = findViewById(R.id.ll_recordatorio);
        tb = findViewById(R.id.toolbar_create_note);
        ll = findViewById(R.id.linear_main);
        sc = findViewById(R.id.scroll_view);

        Intent intent = getIntent();
        if (intent.hasExtra(ID_CREATION_MODE)) {
            if (intent.getStringExtra(ID_CREATION_MODE).equals(CREATION_MODE_1)) { // NOTA
                isOnCreationMode = CREATION_MODE_1;
                setScrollViewParams(true, 45);
                esChecklist = 1;
            } else if (intent.getStringExtra(ID_CREATION_MODE).equals(CREATION_MODE_2)) { // CHECKLIST
                isOnCreationMode = CREATION_MODE_2;
                setScrollViewParams(false, 10);
                esChecklist = 0;
            }
        }

        setLinksEditText();
        getSharedPreferences();
        setViews(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setSharedPreferences();
        if (alarmBuilder != null) alarmBuilder.dismiss();
    }

    /**
     *
     * onDestroy: Calls to UPDATE or INSERT when exiting the activity to store the note
     *
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = getIntent();
        if(isOnCreationMode.equals(CREATION_MODE_1)){              // NOTA
            checkOnDestroyNote(intent);
        } else if (isOnCreationMode.equals(CREATION_MODE_2)) {     // CHECKLIST
            checkOnDestroyCheckList(intent);
        }
    }

    /********************************************************************************************
     *                                                                                           *
     *                                      SET METHODS                                          *
     *                                                                                           *
     *********************************************************************************************/

    /**
     *
     * setScrollViewParams: set of scroll view parameters
     *
     * */
    private void setScrollViewParams(final boolean isSet, int margin) {
        ScrollView.LayoutParams layoutParams = new ScrollView.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, margin);
        ll.setLayoutParams(layoutParams);
        sc.setEnabled(isSet);
    }

    /**
     *
     * setViews: set of the checklist recyclerView or notes fields depending on the CREATION_MODE
     *
     * */
    private void setViews(Intent intent) {
        if (isOnCreationMode.equals(CREATION_MODE_1)) { // NOTA
            mToolbarCreationMode.setBackground(getDrawable(R.drawable.ic_check_box));
            mRecyclerView.setVisibility(View.GONE);
            mTextViewAddElem.setVisibility(View.GONE);
            if (intent.hasExtra(UPDATE_NOTE)) {
                loadQuery(intent);
            } else {
                mEditTextDescription.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        } else if(isOnCreationMode.equals(CREATION_MODE_2)) {  // CHECKLIST
            mToolbarCreationMode.setBackground(getDrawable(R.drawable.ic_note));
            mEditTextDescription.setVisibility(View.GONE);
            if (intent.hasExtra(UPDATE_NOTE)) {
                setRecyclerView(editTextListCheckBox, isCheckedListCheckBox, true);
                loadQuery(intent);
            } else {
                setRecyclerView(editTextListCheckBox, isCheckedListCheckBox, false);
                editTextListCheckBox.add("");
                isCheckedListCheckBox.add(false);
                mAdapter.setBinds(editTextListCheckBox, isCheckedListCheckBox);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    /**
     *
     * checkOnDestroyNote: helper method that checks on the note to delete, insert or update it.
     *
     * */
    private void checkOnDestroyNote(Intent intent) {
        if (mEditTextDescription.getText().toString().isEmpty()
                && mEditTextTitle.getText().toString().isEmpty()) {
            deleteNote(intent);
            makeToast(getString(R.string.error_toast_delete));
        }

        // if(!isOnDelete) {
            if (intent.hasExtra(UPDATE_NOTE)) {
                if (mEditTextDescription.getText().toString().equals(mNote.getDescripcion())
                        && mEditTextTitle.getText().toString().equals(mNote.getTitulo())
                        && mNote.getArchivada() == checkArchivada(archivedPressed)) {
                    if (intent.hasExtra(MyWorkerNotifier.FINISHED)
                            && getIntent().getBooleanExtra(MyWorkerNotifier.FINISHED, false)) {
                        NoteEntity note = getNote();
                        note.setId(intent.getIntExtra(NOTE_ID, -1));
                        DatabaseQueries.updateQuery(note, this);
                    }
                } else {
                    if (mEditTextDescription.getText().toString().isEmpty()
                            && mEditTextTitle.getText().toString().isEmpty()) {
                        deleteNote(intent);
                    } else {
                        NoteEntity note = getNote();
                        note.setId(intent.getIntExtra(NOTE_ID, -1));
                        DatabaseQueries.updateQuery(note, this);
                    }
                }
            } else {
                 if (!mEditTextDescription.getText().toString().isEmpty()
                        || !mEditTextTitle.getText().toString().isEmpty()) {
                     Log.d("00", "INSERT: ID " + getNote().getId());
                     DatabaseQueries.insertQuery(getNote(), this);
                 }
        }
        //}
    }

    /**
     *
     * checkOnDestroyCheckList: elper method that checks on the checklist to delete, insert or update it.
     *
     * */
    private void checkOnDestroyCheckList(Intent intent) {
        int sizeOfList = mAdapter.getCheckedList().size();
        int isEmpty = 0;
        ArrayList<String> et = mAdapter.getTextList();
        for (int x = 0; x < et.size(); x++) {
            if (et.get(x).equals("")) isEmpty++;
        }

        if ((sizeOfList <= 1 || isEmpty == et.size()) && mEditTextTitle.getText().toString().isEmpty()) {
            deleteNote(intent);
            makeToast(getString(R.string.error_toast_delete_checklist));
        } else {
            // if (!isOnDelete) {
                String[] res = getCurrentDescriptionAndCheckBoxes();
                String description = res[0];
                String areChecked = res[1];

                if (intent.hasExtra(UPDATE_NOTE)) {
                    if (description.equals(mNote.getDescripcion())
                            && areChecked.equals(mNote.getCheckbox())
                            && mEditTextTitle.getText().toString().equals(mNote.getTitulo())
                            && mNote.getArchivada() == checkArchivada(archivedPressed)) {
                        if (intent.hasExtra(MyWorkerNotifier.FINISHED)
                                && getIntent().getBooleanExtra(MyWorkerNotifier.FINISHED, false)) {
                            NoteEntity note = getNote();
                            note.setId(intent.getIntExtra(NOTE_ID, -1));
                            DatabaseQueries.updateQuery(note, this);
                        }
                    } else {
                        if (et.size() <= 1 && mEditTextTitle.getText().toString().isEmpty()) {
                            deleteNote(intent);
                        } else {
                            NoteEntity note = getNote();
                            note.setId(intent.getIntExtra(NOTE_ID, -1));
                            DatabaseQueries.updateQuery(note, this);
                        }
                    }
                } else {
                    if (et.size() > 1
                            || !mEditTextTitle.getText().toString().isEmpty()) {
                        DatabaseQueries.insertQuery(getNote(), this);
                    }
                }
            // }
        }
    }

    private void setRecyclerView(ArrayList<String> text, ArrayList<Boolean> checks, boolean mode) {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(lm);

        mAdapter = new CreateCheckListAdapter(getApplicationContext(), text, checks, mode);

        touchHelper = new ItemTouchHelper(new SwipeDragAndDropChecklist(this, mAdapter));
        touchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void getSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        archivedPressed = preferences.getBoolean(ID_BUNDLE_ARCHIVED, true);
        if (archivedPressed) archivedPressed = false;
    }

    private void setSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(ID_BUNDLE_ARCHIVED, archivedPressed);
        editor.apply();
    }

    /**
     *
     * setLinksEditText: this method creates the custom color links and makes them clickable on the fly
     * It is capable of this thanks to commonUtils/LinksManagement
     *
     * */
    private void setLinksEditText(){
        mEditTextDescription.setAutoLinkMask(Linkify.WEB_URLS);
        mEditTextDescription.setMovementMethod(LinksManagement.getInstance());
        Linkify.addLinks(mEditTextDescription, Linkify.WEB_URLS);

        mEditTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) { Linkify.addLinks(s, Linkify.WEB_URLS); }
        });
    }

    /**
     *
     * setFields: Getter of the fields of the note on UPDATE MODE and set the UI with those fields
     *
     * */
    private void setFields(NoteEntity note) {
        mEditTextTitle.setText(note.getTitulo());
        if (note.getArchivada() == 0) { archivedPressed = false; }
        else { archivedPressed = true; }

        if (isOnCreationMode.equals(CREATION_MODE_1)) {      // NOTA
            mEditTextDescription.setText(note.getDescripcion());
        } else if(isOnCreationMode.equals(CREATION_MODE_2)) {   // CHECKLIST
            String[] description = note.getDescripcion().split(DELIMITER);
            String[] checkboxes = note.getCheckbox().split(DELIMITER);
            ArrayList<String> texts = new ArrayList<>(Arrays.asList(description));
            ArrayList<String> checks = new ArrayList<>(Arrays.asList(checkboxes));

            if (checkboxes.length > description.length) {
                int diff = checkboxes.length - description.length;
                for (int x = 0; x < diff; x++) texts.add("");
            }

            transformDescriptionAndCheckbox(texts.toArray(new String[0]), checks.toArray(new String[0]));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        mMenu = menu;
        if (archivedPressed) { mMenu.getItem(1).setIcon(getDrawable(R.drawable.ic_unarchive)); }
        else { mMenu.getItem(1).setIcon(getDrawable(R.drawable.ic_archive)); }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = getIntent();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_archive_note:
                if (archivedPressed) {
                    archivedPressed = false;
                    mMenu.getItem(1).setIcon(getDrawable(R.drawable.ic_archive));
                    if (isOnCreationMode.equals(CREATION_MODE_1)) { // NOTA
                        makeToast("Nota desarchivada");
                    } else if (isOnCreationMode.equals(CREATION_MODE_2)) { // CHECKLIST
                        makeToast("Lista desarchivada");
                    }
                } else {
                    archivedPressed = true;
                    mMenu.getItem(1).setIcon(getDrawable(R.drawable.ic_unarchive));
                    if (isOnCreationMode.equals(CREATION_MODE_1)) { // NOTA
                        makeToast("Nota archivada");
                    } else if (isOnCreationMode.equals(CREATION_MODE_2)) { // CHECKLIST
                        makeToast("Lista archivada");
                    }
                }
                break;
            case R.id.menu_eliminar_nota:
                isOnDelete = true;
                if (isOnCreationMode.equals(CREATION_MODE_1)) { // NOTA
                    if (intent.hasExtra(UPDATE_NOTE)) {
                        titleOnDelete = mEditTextTitle.getText().toString();
                        descriptionOnDelete = mEditTextDescription.getText().toString();
                        mEditTextTitle.setText("");
                        mEditTextDescription.setText("");
                        makeSnackBar( "Nota descartada");
                    } else {
                        makeToast(getResources().getString(R.string.error_toast_delete));
                        finish();
                    }
                } else if (isOnCreationMode.equals(CREATION_MODE_2)) { // CHECKLIST
                    if (intent.hasExtra(UPDATE_NOTE)) {
                        titleOnDelete = mEditTextTitle.getText().toString();
                        String[] res = getCurrentDescriptionAndCheckBoxes();
                        descriptionOnDelete = res[0];
                        checksOnDelete = res[1];
                        mEditTextTitle.setText("");
                        mAdapter.setBinds(new ArrayList<String>(), new ArrayList<Boolean>());
                        makeSnackBar("Lista descartada");
                    } else {
                        makeToast(getResources().getString(R.string.error_toast_delete));
                        finish();
                    }
                }
                break;
        }
        return true;
    }

    /**
     *
     * getNote(): get all fields of the note para UPDATE o INSERT
     *
     * */
    private NoteEntity getNote() {
        final String title = mEditTextTitle.getText().toString();
        String description = "";
        String areChecked = "";
        final Date date = new Date();
        int archivada = checkArchivada(archivedPressed);

        if (isOnCreationMode.equals(CREATION_MODE_1)) {      // NOTA
            description = mEditTextDescription.getText().toString();
            areChecked = "false";
        } else if(isOnCreationMode.equals(CREATION_MODE_2)) {   // CHECKLIST
            String[] res = getCurrentDescriptionAndCheckBoxes();
            description = res[0];
            areChecked = res[1];
        }

        return new NoteEntity(title, description, areChecked, date, archivada, esChecklist,
                TAG, tieneRecordatorio, textTimeOnNote, textDateOnNote);
    }

    private String[] getCurrentDescriptionAndCheckBoxes(){
        ArrayList<String> et = mAdapter.getTextList();
        ArrayList<Boolean> ck = mAdapter.getCheckedList();

        for (int x = 0; x < et.size(); x++) {
            if (et.get(x).trim().isEmpty()) {
                et.remove(x);
                ck.remove(x);
            }
        }

        String[] res = {TextUtils.join(DELIMITER, et), TextUtils.join(DELIMITER, ck)};
        return res;
    }

    /********************************************************************************************
     *                                                                                           *
     *                                     FUNCTIONALITY METHODS                                 *
     *                                                                                           *
     *********************************************************************************************/

    @Override
    public void onBackPressed() {
        View v = getCurrentFocus();
        if (v instanceof EditText) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isOnCreationMode.equals(CREATION_MODE_2)) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     *
     * transformDescriptionAndCheckbox: transforms strings to match the type of the Adapter of the RecyclerView
     * only for checklists
     *
     * */
    private void transformDescriptionAndCheckbox(String[] desc, String [] checks) {
        boolean[] checkedBoxes = new boolean[checks.length];
        for (int x = 0; x < checks.length; x++) {
            if (checks[x].equals("true")) checkedBoxes[x] = true;
            else checkedBoxes[x] = false;
        }
        int sizeOfList = checks.length;

        ArrayList<String> et = mAdapter.getTextList();
        ArrayList<Boolean> ck = mAdapter.getCheckedList();
        for (int x = 0; x < sizeOfList; x++) {
            et.add(desc[x]);
            ck.add(checkedBoxes[x]);
        }
        mAdapter.setBinds(et, ck);
    }

    private int checkArchivada(boolean arc) {
        if (arc) return 1;
        else return 0;
    }

    /**
     *
     * loadQuery: SELECT * FROM note WHERE id = :id
     *
     * */
    private void loadQuery(final Intent intent) {
        int noteID = intent.getIntExtra(NOTE_ID, -1);

        Log.d("aID", "ID de la NOTA: " + noteID);

        final LiveData<NoteEntity> note = NoteArtDatabase.getsInstance(getApplicationContext())
                .noteDao().loadNote(noteID);
        note.observe(this, new Observer<NoteEntity>() {
            @Override
            public void onChanged(@Nullable NoteEntity n) {
                note.removeObserver(this);
                mNote = n;

                if (getIntent().hasExtra(MyWorkerNotifier.FINISHED)
                        && getIntent().getBooleanExtra(MyWorkerNotifier.FINISHED, false)) {
                    setViewsRecordatorioOff();
                } else {
                    TAG = n.getTag();
                }

                if (mNote.getRecordatorio() == 1) {     // Hay Recordatorio
                    mLinearLayoutRecordatorio.setVisibility(View.VISIBLE);
                    String text = "Recordatorio: " + mNote.getFecha_recordatorio() + " a las " + mNote.getHora_recordatorio();
                    mTextViewRecordatorio.setText(text);
                } else if (mNote.getRecordatorio() == 0) {    // No hay Recordatorio
                    mLinearLayoutRecordatorio.setVisibility(View.GONE);
                }
                setFields(n);
            }
        });
    }

    /**
     *
     * loadQuery: DELETE FROM note WHERE id = :id
     *
     * */
    private void deleteNote(Intent b) {
        NoteEntity note = getNote();
        note.setId(b.getIntExtra(NOTE_ID, -1));
        DatabaseQueries.deleteQuery(note, this);
    }

    /**
     *
     * updateNoteOnRecordatorio: UPDATE on the note on adding or updating an alarm
     *
     * */
    private void updateNoteOnRecordatorio(Intent b) {
        NoteEntity note = getNote();
        note.setId(b.getIntExtra(NOTE_ID, -1));
        note.setTag(TAG);
        note.setRecordatorio(tieneRecordatorio);
        note.setHora_recordatorio(textTimeOnNote);
        note.setFecha_recordatorio(textDateOnNote);
        mNote = note;
        DatabaseQueries.updateQuery(note, this);
    }

    private void makeSnackBar(String msg) {
        final Snackbar snackbar = Snackbar.make(ll, msg, Snackbar.LENGTH_LONG);
        snackbar.setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditTextTitle.setText(titleOnDelete);
                if (isOnCreationMode.equals(CREATION_MODE_1)) {
                    mEditTextDescription.setText(descriptionOnDelete);
                } else if (isOnCreationMode.equals(CREATION_MODE_2)){
                    transformDescriptionAndCheckbox(descriptionOnDelete.split(DELIMITER), checksOnDelete.split(DELIMITER));
                }
            }
        });
        snackbar.show();
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onShown(Snackbar sb) {
                tb.setTranslationY(-120);
                super.onShown(sb);
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                tb.setTranslationY(0);
                titleOnDelete = null;
                descriptionOnDelete = null;
            }
        });

        View snack = snackbar.getView();
        TextView snackText = snack.findViewById(com.google.android.material.R.id.snackbar_action);
        snackText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void makeToast(String msg) {
        if (mToast != null) mToast.cancel();
        mToast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /********************************************************************************************
     *                                                                                           *
     *                                  TOOLBAR METHODS                                          *
     *                                                                                           *
     *********************************************************************************************/

    public void changeCreationMode(View view) {
        if (isOnCreationMode.equals(CREATION_MODE_1)) {
            isOnCreationMode = CREATION_MODE_2;
            esChecklist = 0;
            mToolbarCreationMode.setBackground(getDrawable(R.drawable.ic_note));

            String[] textsEachLine = mEditTextDescription.getText().toString().split("\n");
            ArrayList<String> texts = new ArrayList<>();
            ArrayList<Boolean> checks = new ArrayList<>();

            if (textsEachLine.length == 1 && (textsEachLine[0].isEmpty() || textsEachLine[0].trim().isEmpty())) {
                texts.add("");
                checks.add(false);
            } else {
                for (int x = 0; x < textsEachLine.length; x++) {
                    if (!textsEachLine[x].equals("\n") && textsEachLine[x] != null
                            && !textsEachLine[x].trim().isEmpty()) {
                        texts.add(textsEachLine[x]);
                        checks.add(false);
                    }
                }
            }

            mRecyclerView.setVisibility(View.VISIBLE);
            mTextViewAddElem.setVisibility(View.VISIBLE);
            mEditTextDescription.setVisibility(View.GONE);
            setScrollViewParams(false, 10);

            setRecyclerView(texts, checks, false);
        } else if (isOnCreationMode.equals(CREATION_MODE_2)) {
            isOnCreationMode = CREATION_MODE_1;
            esChecklist = 1;
            mToolbarCreationMode.setBackground(getDrawable(R.drawable.ic_check_box));

            ArrayList<String> et = mAdapter.getTextList();
            String textParsed = TextUtils.join("\n", et);

            mRecyclerView.setVisibility(View.GONE);
            mTextViewAddElem.setVisibility(View.GONE);
            mEditTextDescription.setVisibility(View.VISIBLE);
            mEditTextDescription.requestFocus();
            mEditTextDescription.setText(textParsed);
            mEditTextDescription.setSelection(mEditTextDescription.getText().length());

            setScrollViewParams(true, 45);
        }
    }

    public void addElemToChecklist(View view) {
        mAdapter.addNewElementOnButton();
        View v = getCurrentFocus();
        if (v instanceof EditText) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void copyContentToClipboard(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String text = "";
        if (isOnCreationMode.equals(CREATION_MODE_1)) {
            text = mEditTextDescription.getText().toString();
        } else {
            text = TextUtils.join("\n", mAdapter.getTextList());
        }
        ClipData clip = ClipData.newPlainText("descripcion", text);
        clipboard.setPrimaryClip(clip);
        mEditTextDescription.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        makeToast("Nota copiada al portapapeles");
    }

    /********************************************************************************************
     *                                                                                           *
     *                              ALARM & WORK MANAGER METHODS                                 *
     *                                                                                           *
     *********************************************************************************************/

    /**
     *
     * addAlarmToNote: creates the Dialog
     *
     * */
    public void addAlarmToNote(View view) {
        View v = getCurrentFocus();
        if (v instanceof EditText) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            v.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        View customView = getLayoutInflater().inflate(R.layout.create_alert, null);
        mTextViewTimePicker = customView.findViewById(R.id.et_show_time);
        mTextViewDatePicker = customView.findViewById(R.id.et_show_date);
        mButtonCancelRecordatorio = customView.findViewById(R.id.button_cancel);
        mButtonAñadirRecordatorio = customView.findViewById(R.id.button_añadir);

        if (mLinearLayoutRecordatorio.getVisibility() == View.VISIBLE) {
            if (getIntent().hasExtra(UPDATE_NOTE)) {
                if (mNote.getHora_recordatorio() != null && mNote.getFecha_recordatorio() != null) {
                    mTextViewTimePicker.setText(mNote.getHora_recordatorio());
                    mTextViewDatePicker.setText(mNote.getFecha_recordatorio());
                    mButtonAñadirRecordatorio.setText("Modificar");
                    mButtonCancelRecordatorio.setVisibility(View.VISIBLE);
                }
            } else {
                mTextViewTimePicker.setText(textTimeOnNote);
                mTextViewDatePicker.setText(textDateOnNote);
                mButtonAñadirRecordatorio.setText("Modificar");
                mButtonCancelRecordatorio.setVisibility(View.VISIBLE);
            }
        } else {
            mButtonAñadirRecordatorio.setText("Añadir");
            mButtonCancelRecordatorio.setVisibility(View.GONE);
        }

        AlertDialog.Builder alarm = new AlertDialog.Builder(this)
                .setView(customView);
        alarmBuilder = alarm.create();
        alarmBuilder.show();
    }

    /**
     *
     * addAlarm: establishes the worker's parameter depending on DatePicker and TimePicker
     *
     * */
    public void addAlarm(View view) {
        if (mTextViewDatePicker.getText().toString().isEmpty() ||
                mTextViewTimePicker.getText().toString().isEmpty()) {
            makeToast("Rellena los campos");
        } else {
            String title = mEditTextTitle.getText().toString();
            final long toWait = calculateDurationToAlert();

            Data data;
            if (getIntent().hasExtra(UPDATE_NOTE)) {
                data = new Data.Builder()
                        .putString(MyWorkerNotifier.KEY_WORKER_TITLE_NOTIFICATION, title)
                        .putInt(MyWorkerNotifier.KEY_WORKER_ID_NOTIFICATION, mNote.getId())
                        .putInt(MyWorkerNotifier.KEY_WORKER_MODE_NOTIFICATION, mNote.getEsChecklist())
                        .build();

                OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorkerNotifier.class)
                        .setInputData(data)
                        .setInitialDelay(toWait, TimeUnit.MILLISECONDS)
                        .build();

                if (mNote.getTag() == null) { TAG = work.getId().toString(); }
                else { TAG = mNote.getTag(); }

                WorkManager.getInstance(this)
                        .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, work);

                // Solo funciona si al notificarse el usuario permanece en esta actividad
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(UUID.fromString(TAG))
                        .observe(this, new Observer<WorkInfo>() {
                            @Override
                            public void onChanged(@Nullable WorkInfo workInfo) {
                                if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                    setViewsRecordatorioOff();
                                }
                            }
                        });

                makeToast("Recordatorio añadido");

                tieneRecordatorio = 1;
                mLinearLayoutRecordatorio.setVisibility(View.VISIBLE);
                String text = "Recordatorio: " + textDateOnNote + " a las " + textTimeOnNote;
                mTextViewRecordatorio.setText(text);
                alarmBuilder.dismiss();

                updateNoteOnRecordatorio(getIntent());

                // TODO: Actualizar la UI tras la muestra de una notificación en referencia a una nota

                // TODO: Hacer posible que se puedan agregar recordatorios en una nota recien insertada
                //      Hay problema con el ID, no puede ser 0 porque va en función del getAdapterPosition()
            } else {
                makeToast("Para agregar un recordatorio, guarda la nota primero");
                /*
                data = new Data.Builder()
                        .putString(MyWorkerNotifier.KEY_WORKER_TITLE_NOTIFICATION, title)
                        .putInt(MyWorkerNotifier.KEY_WORKER_ID_NOTIFICATION, 0)
                        .putInt(MyWorkerNotifier.KEY_WORKER_MODE_NOTIFICATION, esChecklist)
                        .build();
                */
            }
        }
    }

    public void cancelAlarm(View view) {
        WorkManager.getInstance(this).cancelWorkById(UUID.fromString(TAG));
        setViewsRecordatorioOff();
        alarmBuilder.dismiss();
        makeToast("Recordatorio cancelado.");
    }

    /**
     *
     * setViewsRecordatorioOff: helper method to make sure the alarm is off
     *
     * */
    private void setViewsRecordatorioOff() {
        tieneRecordatorio = 0;
        textTimeOnNote = null;
        textDateOnNote = null;
        TAG = null;
        mLinearLayoutRecordatorio.setVisibility(View.GONE);
        updateNoteOnRecordatorio(getIntent());
    }

    private long calculateDurationToAlert() {
        final String scheduledDateAux = mTextViewDatePicker.getText().toString();
        final String[] splitDate = scheduledDateAux.split("/");
        final String scheduledDate = splitDate[0].concat("/" + (Integer.parseInt(splitDate[1])-1) + "/" + splitDate[2]);
        final String scheduledTimeAux = mTextViewTimePicker.getText().toString();
        final String[] splitTime = scheduledTimeAux.split(":");
        if (splitTime[1].charAt(0) == '0') splitTime[1] = splitTime[1].substring(1);
        final String scheduledTime = splitTime[0].concat(":" + splitTime[1]);

        Calendar currentCalendar = Calendar.getInstance();
        final String currentYear = Integer.toString(currentCalendar.get(Calendar.YEAR));
        final String currentMonth = Integer.toString(currentCalendar.get(Calendar.MONTH));
        final String currentDay = Integer.toString(currentCalendar.get(Calendar.DAY_OF_MONTH));
        final String[] currentDate = {currentDay, currentMonth, currentYear};
        final String currentHour = Integer.toString(currentCalendar.get(Calendar.HOUR_OF_DAY));
        final String currentMinute = Integer.toString(currentCalendar.get(Calendar.MINUTE));
        final String[] currentTime = {currentHour, currentMinute};

        final String date = TextUtils.join("/", currentDate);
        final String time = TextUtils.join(":", currentTime);

        givenDateString = scheduledDate.concat(" " + scheduledTime);
        final String currentDateString = date.concat(" " + time);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy hh:mm");
        try {
            Date scheduled = sdf.parse(givenDateString);
            Date current = sdf.parse(currentDateString);
            return scheduled.getTime() - current.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void openTimePickerDialog(View view) {
        TimePickerDialog mTimePicker;
        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        final int minute = mcurrentTime.get(Calendar.MINUTE);

        mTimePicker = new TimePickerDialog(this, R.style.PickerDialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourSel, int minuteSel) {
                String text = Integer.toString(hourSel) + ":";
                if (minuteSel >= 0 && minuteSel <= 9) text = text.concat("0" + Integer.toString(minuteSel));
                else text = text.concat(Integer.toString(minuteSel));
                mTextViewTimePicker.setText(text);
                mTextViewTimePicker.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextContent));
                textTimeOnNote = text;
            }
        }, hour, minute, true);
        mTimePicker.show();
    }

    public void openDatePickerDialog(View view) {
       DatePickerDialog mDatePicker;
        Calendar mcurrentDate = Calendar.getInstance();
        final int mYear = mcurrentDate.get(Calendar.YEAR);
        final int mMonth = mcurrentDate.get(Calendar.MONTH);
        final int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        mDatePicker = new DatePickerDialog(this, R.style.PickerDialogTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                String text = Integer.toString(selectedday) + "/" + Integer.toString(selectedmonth+1) + "/" + Integer.toString(selectedyear);
                mTextViewDatePicker.setText(text);
                mTextViewDatePicker.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextContent));
                textDateOnNote = text;
            }
        }, mYear, mMonth, mDay);
        mDatePicker.show();
    }
}

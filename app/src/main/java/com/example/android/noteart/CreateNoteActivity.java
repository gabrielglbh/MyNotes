package com.example.android.noteart;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.noteart.adapters.CreateCheckListAdapter;
import com.example.android.noteart.commonUtils.LinksManagement;
import com.example.android.noteart.database.NoteArtDatabase;
import com.example.android.noteart.database.NoteEntity;
import com.example.android.noteart.commonUtils.DatabaseQueries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText mEditTextTitle, mEditTextDescription;
    private TextView mToolbarCreationMode, mTextViewAddElem;
    private LinearLayout ll;
    private ScrollView sc;
    private android.support.v7.widget.Toolbar tb;
    private NoteEntity mNote;
    private Toast mToast;
    private Menu mMenu;
    private RecyclerView mRecyclerView;
    private CreateCheckListAdapter mAdpater;

    private final String NOTE_ID = "id_nota";
    private final String UPDATE_NOTE = "update_nota";
    private final String ID_BUNDLE_ARCHIVED = "archivada";

    private final String ID_CREATION_MODE = "creationMode";
    private final String CREATION_MODE_1 = "nota";
    private final String CREATION_MODE_2 = "checklist";

    private final String DELIMITER = "#/@/#--";

    private boolean archivedPressed = false;
    private boolean isOnDelete = false;
    private String titleOnDelete, descriptionOnDelete, checksOnDelete;
    private String isOnCreationMode;
    private int esChecklist;

    private ArrayList<String> editTextListCheckBox = new ArrayList<>();
    private ArrayList<Boolean> isCheckedListCheckBox = new ArrayList<>();

    /**
     *
     * onCreate: Si se hace la actividad desde una nota: LOAD y UPDATE
     *      Si se hace desde el FAB: INSERT
     *
     *      Se recoge información del intent si se accede por modo UPDATE
     *
     *      Se recoge la información del fichero sharedPreferences
     *
     *      Se crea un listener para el teclado para los editText
     *
     *      Para quitar el focus del teclado en el INSERT note, añadir:
     *          focusable y focusableonTouch = true en el LinearLayout parent
     *
     *      Se añade la edición y creación de links/URLs clickables
     *
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        mEditTextTitle = findViewById(R.id.et_edit_note_title);
        mEditTextDescription = findViewById(R.id.et_edit_note_description);
        mRecyclerView = findViewById(R.id.rv_main_checklist);
        mToolbarCreationMode = findViewById(R.id.toolbar_creation_mode);
        mTextViewAddElem = findViewById(R.id.toolbar_add_elem);
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
    }

    /**
     *
     * onDestroy: Se llama a UPDATE/DELETE o INSERT al destruir la actividad
     *
     * Si al acceder la nota no se ha modificado nada, se deja igual
     * Si se está accediendo desde UPDATE mode:
     *      Si se vacian los campos, se hace DELETE
     *      Si se cambia algo, se hace UPDATE
     * Si no se accede desde el UPDATE mode: INSERT
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
     *                               METODOS SET PARA ON CREATE                                  *
     *                                                                                           *
     *********************************************************************************************/

    /**
     *
     * setScrollViewParams: set de los distintos parámetros de la scrollview para hacer smooth
     * transitions
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
     * setViews: Se hace set de los recycler view o de las notas al empezar la actividad
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
                mAdpater.setBinds(editTextListCheckBox, isCheckedListCheckBox);
            }
        }
    }

    /**
     *
     * checkOnDestroyNote: Hace check de la nota para descartarla, updatearla o insertarla
     *
     * */
    private void checkOnDestroyNote(Intent intent) {
        if (mEditTextDescription.getText().toString().isEmpty()
                && mEditTextTitle.getText().toString().isEmpty()) {
            deleteNote(intent);
            makeToast(getString(R.string.error_toast_delete));
        }

        if(!isOnDelete) {
            if (intent.hasExtra(UPDATE_NOTE)) {
                if (mEditTextDescription.getText().toString().equals(mNote.getDescripcion())
                        && mEditTextTitle.getText().toString().equals(mNote.getTitulo())
                        && mNote.getArchivada() == checkArchivada(archivedPressed)) {
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
                    DatabaseQueries.insertQuery(getNote(), this);
                }
            }
        }
    }

    /**
     *
     * checkOnDestroyCheckList: Hace check de la checklist para descartarla, updatearla o insertarla
     *
     * */
    private void checkOnDestroyCheckList(Intent intent) {
        int sizeOfList = mAdpater.getCheckedList().size();
        int isEmpty = 0;
        ArrayList<String> et = mAdpater.getTextList();
        for (int x = 0; x < et.size(); x++) {
            if (et.get(x).equals("")) isEmpty++;
        }

        if ((sizeOfList <= 1 || isEmpty == et.size()) && mEditTextTitle.getText().toString().isEmpty()) {
            deleteNote(intent);
            makeToast(getString(R.string.error_toast_delete_checklist));
        } else {
            if (!isOnDelete) {
                String[] res = getCurrentDescriptionAndCheckBoxes();
                String description = res[0];
                String areChecked = res[1];

                if (intent.hasExtra(UPDATE_NOTE)) {
                    if (description.equals(mNote.getDescripcion())
                            && areChecked.equals(mNote.getCheckbox())
                            && mEditTextTitle.getText().toString().equals(mNote.getTitulo())
                            && mNote.getArchivada() == checkArchivada(archivedPressed)) {
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
            }
        }
    }

    /**
     *
     * setRecyclerView: Crea lo necesario para la administración del RecyclerView
     * Para crear listas dinámicas quitar setHasFixedSize
     *
     * */
    private void setRecyclerView(ArrayList<String> text, ArrayList<Boolean> checks, boolean mode) {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(lm);

        mAdpater = new CreateCheckListAdapter(this, text, checks, mode);

        mRecyclerView.setAdapter(mAdpater);
    }

    /**
     *
     * getSharedPreferences: se recogen de sharedPreferences el filtro y el orden en onCreate
     *
     * */
    private void getSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        archivedPressed = preferences.getBoolean(ID_BUNDLE_ARCHIVED, true);
    }

    /**
     *
     * setSharedPreferences: se escribe el archivePressed en onStop en sharedPreferences
     *
     * */
    private void setSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(ID_BUNDLE_ARCHIVED, archivedPressed);
        editor.apply();
    }

    /**
     *
     * setLinksEditText: Crea los links a medida que se escribe el texto, además de hacerlos
     * clickables dentro del EditText gracias a la clase LinksManagement
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
     * setFields: Getter de los campos de Room para la actividad en UPDATE
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

    /**
     *
     * onOptionsItemSelected: Hacer UPDATE o INSERT
     *
     * También se hace el handler del boton de ir atrás
     *
     * */
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
                        mAdpater.setBinds(new ArrayList<String>(), new ArrayList<Boolean>());
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

        return new NoteEntity(title, description, areChecked, date, archivada, esChecklist);
    }

    /**
     *
     * getCurrentDescriptionAndCheckBoxes: recoge los textos de las checklist
     *
     * */
    private String[] getCurrentDescriptionAndCheckBoxes(){
        ArrayList<String> et = mAdpater.getTextList();
        ArrayList<Boolean> ck = mAdpater.getCheckedList();

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
     *                                  METODOS DE FUNCIONALIDAD                                 *
     *                                                                                           *
     *********************************************************************************************/

    /**
     *
     * dispatchTouchEvent y onBackPressed: Ayuda que al darle atrás de la checklist,
     * se pierda el focus de los editText y, en consecuencia, se pueda guardar el texto correctamente
     *
     * Solo está activo en el modo de checklists
     *
     * */
    @Override
    public void onBackPressed() {
        if (isOnCreationMode.equals(CREATION_MODE_2)) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
        super.onBackPressed();
    }

    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isOnCreationMode.equals(CREATION_MODE_2) && !isAddElementPressed) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (v instanceof EditText) {
                    Rect outRect = new Rect();
                    v.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
            isAddElementPressed = false;
        }
        return super.dispatchTouchEvent(event);
    }
    */

    /**
     *
     * transformDescriptionAndCheckbox: transforma el String a un ArrayList<> y hace set
     * de las listas en el adapter
     *
     * */
    private void transformDescriptionAndCheckbox(String[] desc, String [] checks) {
        boolean[] checkedBoxes = new boolean[checks.length];
        for (int x = 0; x < checks.length; x++) {
            if (checks[x].equals("true")) checkedBoxes[x] = true;
            else checkedBoxes[x] = false;
        }
        int sizeOfList = checks.length;

        ArrayList<String> et = mAdpater.getTextList();
        ArrayList<Boolean> ck = mAdpater.getCheckedList();
        for (int x = 0; x < sizeOfList; x++) {
            et.add(desc[x]);
            ck.add(checkedBoxes[x]);
        }
        mAdpater.setBinds(et, ck);
    }

    /**
     *
     * checkArchivada: Método auxiliar para convertir booleano a int
     *
     * */
    private int checkArchivada(boolean arc) {
        if (arc) return 1;
        else return 0;
    }

    /**
     *
     * changeCreationMode: Cambia el modo de creacion de nota a checklist o de checklist a nota
     *
     * */
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

            ArrayList<String> et = mAdpater.getTextList();
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

    /**
     *
     * addElemToChecklist: Por si el usuario quiere añadir un elemento
     *
     * */
    public void addElemToChecklist(View view) {
        ArrayList<Boolean> checks = mAdpater.getCheckedList();
        if (!checks.contains(false)) {
            mAdpater.addNewElementOnButton();
        } else {
            makeToast("Todavía tienes tareas sin hacer");
        }
    }

    /**
     *
     * loadQuery: SELECT * FROM note WHERE id = :id
     *
     * */
    private void loadQuery(final Intent intent) {
        int noteID = intent.getIntExtra(NOTE_ID, -1);
        final LiveData<NoteEntity> note = NoteArtDatabase.getsInstance(getApplicationContext())
                .noteDao().loadNote(noteID);
        note.observe(this, new Observer<NoteEntity>() {
            @Override
            public void onChanged(@Nullable NoteEntity n) {
                note.removeObserver(this);
                mNote = n;
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
     * makeSnackBar: Crea un toast con una acción para deshacer la eliminación en un tiempo limitado
     *
     * */
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
            }
        });

        View snack = snackbar.getView();
        TextView snackText = snack.findViewById(android.support.design.R.id.snackbar_action);
        snackText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void makeToast(String msg) {
        if (mToast != null) mToast.cancel();
        mToast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

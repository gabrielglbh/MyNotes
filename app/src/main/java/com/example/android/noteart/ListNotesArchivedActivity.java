package com.example.android.noteart;

import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.noteart.adapters.NoteListAdapter;
import com.example.android.noteart.database.NoteEntity;
import com.example.android.noteart.commonUtils.CustomSharedPreferences;
import com.example.android.noteart.commonUtils.DatabaseQueries;
import com.example.android.noteart.commonUtils.DeleteModeOperations;
import com.example.android.noteart.commonUtils.SwipeHandle;

import java.util.ArrayList;

public class ListNotesArchivedActivity extends AppCompatActivity
        implements NoteListAdapter.ListItemClickListener {

    private RecyclerView mRecyclerViewMain;
    private NoteListAdapter mAdapterNote;
    private LinearLayout ll;
    private Menu mMenu;
    private AlertDialog alt, altDelete;

    private String defaultFilter = "Fecha";
    private String defaultOrder = "DESC";
    private int itemClicked = 0, totalCountSelected = 1;
    private ArrayList<NoteEntity> mNoteListSelected = new ArrayList<>();
    private ArrayList<LinearLayout> mTextViewListSelected = new ArrayList<>();
    private ArrayList<Integer> mNotesIdList = new ArrayList<>();
    private boolean isDeleteModeOpen = false;

    private final String orderAsc = "ASC";
    private final String orderDesc = "DESC";

    private String[] ID_BUNDLE = {"priority_archived", "orderBy_archived"};
    private String ID_DELETEMODE_BUNDLE = "onDeleteMode";
    private final String ID_CREATION_MODE = "creationMode";
    private final String CREATION_MODE_1 = "nota";
    private final String CREATION_MODE_2 = "checklist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_archived);

        mRecyclerViewMain = findViewById(R.id.rv_main_archived);
        ll = findViewById(R.id.linear_layout);

        setRecyclerView();
        setFiltersOfBundle();
        getSharedPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFiltersOfBundle();
        isDeleteModeOpen = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        String[] data = {defaultFilter, defaultOrder};
        CustomSharedPreferences.setSharedPreferences(this, ID_BUNDLE, data);
    }

    /********************************************************************************************
     *                                                                                           *
     *                               METODOS SET PARA ON CREATE                                  *
     *                                                                                           *
     *********************************************************************************************/

    /**
     *
     * getSharedPreferences: se recogen de sharedPreferences el filtro y el orden en onCreate
     *
     * */
    private void getSharedPreferences() {
        android.content.SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultFilter = preferences.getString(ID_BUNDLE[0], "");
        defaultOrder = preferences.getString(ID_BUNDLE[1], "");
    }

    /**
     *
     * setFiltersOfBundle: Se llama después de onResume o onCreate para hacer la query de
     * loadNotes en base a los valores guardados por el usuario
     *
     * */
    private void setFiltersOfBundle() {
        final String TITULO = "Título";
        final String FECHA = "Fecha";
        switch (defaultFilter) {
            case FECHA:
                itemClicked = 0;
                break;
            case TITULO:
                itemClicked = 1;
                break;
        }
        loadNotes();
    }

    /**
     *
     * setRecyclerView: Crea lo necesario para la administración del RecyclerView
     *
     * */
    private void setRecyclerView() {
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mRecyclerViewMain.setLayoutManager(lm);
        mRecyclerViewMain.setHasFixedSize(true);

        mAdapterNote = new NoteListAdapter(this, this);

        mRecyclerViewMain.setAdapter(mAdapterNote);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeHandle(
                mAdapterNote, getApplicationContext(), 0, "Nota restaurada", true));
        itemTouchHelper.attachToRecyclerView(mRecyclerViewMain);
    }

    /**
     *
     * onCreateOptionsMenu: Crea el menu y hace set del icono de ASC o DESC conforme a deafultOrder
     *
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.archived_menu, menu);
        if (defaultOrder.equals(orderAsc)) {
            menu.getItem(2).setIcon(getDrawable(R.drawable.ic_up));
        } else {
            menu.getItem(2).setIcon(getDrawable(R.drawable.ic_down));
        }
        mMenu = menu;
        return true;
    }

    /**
     *
     * onOptionsItemSelected:
     *  case R.id.menu_filter_by:
     *      Query por filtro
     *
     *  case R.id.menu_order_by:
     *      Query por ASC o DESC
     *
     *  case android.R.id.home:
     *      Vuelta a la actividad principal
     *
     *  case R.id.menu_delete:
     *      Una vez se hayan seleccionado las notas, mostrar AlertDialog
     *
     *  case R.id.menu_deselect_all:
     *      Quitar todas las notas seleccionadas y isDeleteModeOpen = false
     *      Se realiza un .clear() de ArrayList para evitar errores
     *
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int optionMenuClicked = item.getItemId();
        final String[] filterArray = getResources().getStringArray(R.array.filters);

        switch (optionMenuClicked) {
            case R.id.menu_filter_by:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this,
                        R.style.Theme_MaterialComponents_Light_Dialog_Alert);
                builder.setTitle(R.string.order_by_dialog)
                        .setSingleChoiceItems(filterArray,
                                itemClicked,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int filter) {
                                        switch (filter) {
                                            case 0:
                                                itemClicked = 0;
                                                break;
                                            case 1:
                                                itemClicked = 1;
                                                break;
                                        }
                                        defaultFilter = filterArray[filter];
                                        loadNotes();
                                        alt.dismiss();
                                    }
                                });
                alt = builder.create();
                alt.show();
                break;
            case R.id.menu_order_by:
                if (defaultOrder.equals(orderAsc)) {
                    mMenu.getItem(2).setIcon(getDrawable(R.drawable.ic_down));
                    defaultOrder = orderDesc;
                } else {
                    mMenu.getItem(2).setIcon(getDrawable(R.drawable.ic_up));
                    defaultOrder = orderAsc;
                }
                loadNotes();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.menu_delete:
                makeAlertDeleteMode();
                break;
            case R.id.menu_deselect_all:
                totalCountSelected = 1;
                setTitle(getString(R.string.app_name));
                isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(this,
                        mTextViewListSelected, mMenu, mNoteListSelected, true);
                CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
                break;
        }
        return true;
    }

    /********************************************************************************************
     *                                                                                           *
     *                                METODOS DE FUNCIONALIDAD                                   *
     *                                                                                           *
     *********************************************************************************************/

    /**
     *
     * loadNotes: Guardar notas - referencia a loadNotes de DatabaseQueries
     *
     * */
    private void loadNotes() {
        DatabaseQueries.loadNotes(this, defaultFilter, defaultOrder,
                1, mAdapterNote, this);
    }

    /**
     *
     * onElementClicked: Nueva actividad que accede a la nota para hacer un UPDATE de la misma
     *
     *      Si isDeleteModeOpen & la nota ya estaba seleccionada -> Se elimina la nota de la lista
     *          de seleccionadas
     *
     *          Si no estaba seleccionada, se selecciona
     *
     *      Si no isDeleteModeOpen -> Nueva actividad para abrir la nota
     *
     * */
    public void onElementClicked(int id, NoteEntity note, LinearLayout frame) {
        final String NOTE_ID = "id_nota";
        final String UPDATE_NOTE = "update_nota";
        if (isDeleteModeOpen) {
            DeleteModeOperations.changeColorFrame(frame, this, R.color.selectedFrame);
            if (mNotesIdList.contains(id)) {
                totalCountSelected--;
                setTitle(Integer.toString(totalCountSelected));
                DeleteModeOperations.removeNote(id, frame, note,
                        mNotesIdList, mTextViewListSelected, mNoteListSelected);
                DeleteModeOperations.changeColorFrame(frame, this, R.color.colorPrimaryActionBar);
                if (mNotesIdList.isEmpty()) {
                    setTitle(getString(R.string.app_name));
                    totalCountSelected = 1;
                    isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(this,
                            mTextViewListSelected, mMenu, mNoteListSelected, true);
                    CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
                }
            }
            else {
                totalCountSelected++;
                setTitle(Integer.toString(totalCountSelected));
                mNoteListSelected.add(note);
                mTextViewListSelected.add(frame);
                mNotesIdList.add(id);
            }
        } else {
            Intent createNote = new Intent(getApplicationContext(), CreateNoteActivity.class);
            createNote.putExtra(NOTE_ID, id);
            createNote.putExtra(UPDATE_NOTE, true);
            if (note.getEsChecklist() == 1) createNote.putExtra(ID_CREATION_MODE, CREATION_MODE_1);
            else if (note.getEsChecklist() == 0) createNote.putExtra(ID_CREATION_MODE, CREATION_MODE_2);
            startActivity(createNote);
        }
    }

    /**
     *
     * onElementLongClicked: Se lanza el DeleteMode --> Permite seleccionar varios elementos
     * para borrarlos con DELETE y crea los iconos de DELETE y CLOSE en el menu de la actividad
     *
     * */
    @Override
    public void onElementLongClicked(int id, NoteEntity note, LinearLayout frame) {
        isDeleteModeOpen = true;
        setTitle(Integer.toString(totalCountSelected));
        CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
        DeleteModeOperations.changeColorFrame(frame, this, R.color.selectedFrame);

        mNoteListSelected.add(note);
        mTextViewListSelected.add(frame);
        mNotesIdList.add(id);

        mMenu.getItem(0).setVisible(true);
        mMenu.getItem(1).setVisible(true);
        mMenu.getItem(2).setVisible(false);
        mMenu.getItem(3).setVisible(false);
    }

    /**
     *
     * onBackPressed: Override del boton de ir atrás para acabar con el modo de selección
     *
     * */
    @Override
    public void onBackPressed() {
        if (isDeleteModeOpen) {
            isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(
                    this, mTextViewListSelected, mMenu, mNoteListSelected, true);
            CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
        }
        else super.onBackPressed();
    }

    /**
     *
     * makeAlertDeleteMode: Lanza una alerta para avisar de eliminar las notas seleccionadas:
     *
     *      Afirmativo: Se eliminan las notas, se eliminan iconos de DeleteMode y .clear() de las listas
     *
     *      Negativo: Simplemente se hace un dismiss() de la alerta
     *
     * */
    public void makeAlertDeleteMode() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setTitle("¿Quieres eliminar las " + totalCountSelected + " notas seleccionadas?")
                .setMessage(R.string.message_delete)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        altDelete.dismiss();
                    }
                })
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int x = 0; x < mNoteListSelected.size(); x++) {
                            DatabaseQueries.deleteQuery(mNoteListSelected.get(x),
                                    getApplicationContext());
                            DeleteModeOperations.changeColorFrame(mTextViewListSelected.get(x),
                                    getApplicationContext(), R.color.colorPrimaryActionBar);
                        }
                        mMenu.getItem(0).setVisible(false);
                        mMenu.getItem(1).setVisible(false);
                        mMenu.getItem(2).setVisible(true);
                        mMenu.getItem(3).setVisible(true);

                        isDeleteModeOpen = false;
                        CustomSharedPreferences.setSharedPreferencesDeleteMode(getApplicationContext(), ID_DELETEMODE_BUNDLE, isDeleteModeOpen);

                        totalCountSelected = 1;
                        setTitle(getString(R.string.app_name));

                        altDelete.dismiss();
                        makeSnackBar();
                    }
                });
        altDelete = builder.create();
        altDelete.show();
    }

    /**
     *
     * makeSnackBar: Crea un toast con una acción para deshacer la eliminación en un tiempo limitado
     *
     * */
    private void makeSnackBar() {
        final Snackbar snackbar = Snackbar.make(ll, "Notas eliminadas", Snackbar.LENGTH_LONG);
        snackbar.setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int x = 0; x < mNoteListSelected.size(); x++) {
                    DatabaseQueries.insertQuery(mNoteListSelected.get(x),
                            getApplicationContext());
                }
                snackbar.dismiss();
            }
        });
        snackbar.show();
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                mNoteListSelected.clear();
                mTextViewListSelected.clear();
                mNotesIdList.clear();
                isDeleteModeOpen = false;
                CustomSharedPreferences.setSharedPreferencesDeleteMode(getApplicationContext(), ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
            }
        });

        View snack = snackbar.getView();
        TextView snackText = snack.findViewById(android.support.design.R.id.snackbar_action);
        snackText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }
}

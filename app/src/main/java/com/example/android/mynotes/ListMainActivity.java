package com.example.android.mynotes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.mynotes.adapters.NoteListAdapter;
import com.example.android.mynotes.database.NoteEntity;
import com.example.android.mynotes.commonUtils.CustomSharedPreferences;
import com.example.android.mynotes.database.DatabaseQueries;
import com.example.android.mynotes.commonUtils.DeleteModeOperations;
import com.example.android.mynotes.commonUtils.SwipeHandleToArchived;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

public class ListMainActivity extends AppCompatActivity implements NoteListAdapter.ListItemClickListener {

    private RecyclerView mRecyclerViewNotes;
    private NoteListAdapter mAdapterNote;
    private FrameLayout fm;
    private Toolbar tb;
    private Menu mMenu;
    private AlertDialog alt, altDelete;

    private ArrayList<NoteEntity> mNoteListSelected = new ArrayList<>();
    private ArrayList<CardView> mTextViewListSelected = new ArrayList<>();
    private ArrayList<LinearLayout> mTextViewListLLSelected = new ArrayList<>();
    private ArrayList<Integer> mNotesIdList = new ArrayList<>();
    private int itemClicked = 0, totalCountSelected = 1;
    private boolean isDeleteModeOpen = false;
    private final String[] ID_BUNDLE = {"priority", "orderBy"};

    public static String defaultFilter = "Fecha";
    public static String defaultOrder = "DESC";
    public static final String orderAsc = "ASC";
    public static final String orderDesc = "DESC";
    public static final String ID_DELETEMODE_BUNDLE = "onDeleteMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = findViewById(R.id.frame_layout);
        tb = findViewById(R.id.toolbar_main_act);

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
     *                                          SET METHODS                                      *
     *                                                                                           *
     *********************************************************************************************/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager lm = new GridLayoutManager(this, 2);
            mRecyclerViewNotes.setLayoutManager(lm);
            mRecyclerViewNotes.setAdapter(mAdapterNote);
            mAdapterNote.notifyDataSetChanged();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            LinearLayoutManager lm = new LinearLayoutManager(this);
            mRecyclerViewNotes.setLayoutManager(lm);
            mRecyclerViewNotes.setAdapter(mAdapterNote);
            mAdapterNote.notifyDataSetChanged();
        }
    }

    private void getSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultFilter = preferences.getString(ID_BUNDLE[0], "");
        defaultOrder = preferences.getString(ID_BUNDLE[1], "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }


        if (defaultOrder.equals(orderAsc)) {
            menu.getItem(2).setIcon(getDrawable(R.drawable.ic_up));
        } else {
            menu.getItem(2).setIcon(getDrawable(R.drawable.ic_down));
        }
        mMenu = menu;
        return true;
    }

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
            case R.id.menu_notas_archivadas:
                Intent listArchived = new Intent(getApplicationContext(), ListNotesArchivedActivity.class);
                startActivity(listArchived);
                break;
            case R.id.menu_delete:
                makeAlertDeleteMode();
                break;
            case R.id.menu_deselect_all:
                totalCountSelected = 1;
                setTitle(getString(R.string.app_name));
                isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(this,
                        mTextViewListSelected, mTextViewListLLSelected, mMenu, mNoteListSelected, false);
                CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
                break;
        }
        return true;
    }

    /**
     *
     * setFiltersOfBundle: set the filters and order of the queries to match the users
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

    private void setRecyclerView() {
        mRecyclerViewNotes = findViewById(R.id.rv_main);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        mRecyclerViewNotes.setLayoutManager(lm);
        mRecyclerViewNotes.setHasFixedSize(true);

        mAdapterNote = new NoteListAdapter(this, this);

        mRecyclerViewNotes.setAdapter(mAdapterNote);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeHandleToArchived(
                mAdapterNote, getApplicationContext(), 1, "Nota archivada", false));
        itemTouchHelper.attachToRecyclerView(mRecyclerViewNotes);
    }

    /********************************************************************************************
     *                                                                                           *
     *                                      FUNCTIONALITY METHODS                                *
     *                                                                                           *
     *********************************************************************************************/

    private void loadNotes() {
        DatabaseQueries.loadNotes(this, defaultFilter, defaultOrder,0, mAdapterNote, this);
    }

    public void onNewNoteClicked(View view) {
        if (isDeleteModeOpen) {
            isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(getApplicationContext(),
                    mTextViewListSelected, mTextViewListLLSelected, mMenu, mNoteListSelected, false);
        }
        Intent createNote = new Intent(getApplicationContext(), CreateNoteActivity.class);
        createNote.putExtra(CreateNoteActivity.ID_CREATION_MODE, CreateNoteActivity.CREATION_MODE_1);
        startActivity(createNote);
    }

    public void onNewChecklistClicked(View view) {
        if (isDeleteModeOpen) {
            isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(getApplicationContext(),
                    mTextViewListSelected, mTextViewListLLSelected, mMenu, mNoteListSelected, false);
        }
        Intent createCheckList = new Intent(this, CreateNoteActivity.class);
        createCheckList.putExtra(CreateNoteActivity.ID_CREATION_MODE, CreateNoteActivity.CREATION_MODE_2);
        startActivity(createCheckList);
    }

    public void onElementClicked(int id, NoteEntity note, CardView frame, LinearLayout ll) {
        final String NOTE_ID = "id_nota";
        final String UPDATE_NOTE = "update_nota";
        if (isDeleteModeOpen) {
            DeleteModeOperations.changeColorFrame(frame, ll, this, R.color.selectedFrame);
            if (mNotesIdList.contains(id)) {
                totalCountSelected--;
                setTitle(Integer.toString(totalCountSelected));
                DeleteModeOperations.removeNote(id, frame, note,
                        mNotesIdList, mTextViewListSelected, mNoteListSelected);
                DeleteModeOperations.changeColorFrame(frame, ll, this, R.color.colorPrimaryActionBar);
                if (mNotesIdList.isEmpty()) {
                    setTitle(getString(R.string.app_name));
                    totalCountSelected = 1;
                    isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(this,
                            mTextViewListSelected, mTextViewListLLSelected, mMenu, mNoteListSelected, false);
                    CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
                }
            }
            else {
                totalCountSelected++;
                setTitle(Integer.toString(totalCountSelected));
                mNoteListSelected.add(note);
                mTextViewListSelected.add(frame);
                mTextViewListLLSelected.add(ll);
                mNotesIdList.add(id);
            }
        } else {
            Intent createNote = new Intent(getApplicationContext(), CreateNoteActivity.class);
            createNote.putExtra(NOTE_ID, id);
            createNote.putExtra(UPDATE_NOTE, true);
            if (note.getEsChecklist() == 1) createNote.putExtra(CreateNoteActivity.ID_CREATION_MODE, CreateNoteActivity.CREATION_MODE_1);
            else if (note.getEsChecklist() == 0) createNote.putExtra(CreateNoteActivity.ID_CREATION_MODE, CreateNoteActivity.CREATION_MODE_2);
            startActivity(createNote);
        }
    }

    @Override
    public void onElementLongClicked(int id, NoteEntity note, CardView frame, LinearLayout ll) {
        isDeleteModeOpen = true;
        setTitle(Integer.toString(totalCountSelected));
        CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
        DeleteModeOperations.changeColorFrame(frame, ll, this, R.color.selectedFrame);

        mNoteListSelected.add(note);
        mTextViewListSelected.add(frame);
        mTextViewListLLSelected.add(ll);
        mNotesIdList.add(id);

        mMenu.getItem(0).setVisible(true);
        mMenu.getItem(1).setVisible(true);
        mMenu.getItem(2).setVisible(false);
        mMenu.getItem(3).setVisible(false);
        mMenu.getItem(4).setVisible(false);
    }

    @Override
    public void onBackPressed() {
        if (isDeleteModeOpen) {
            isDeleteModeOpen = DeleteModeOperations.deleteModeShutdownNotes(
                    this, mTextViewListSelected, mTextViewListLLSelected, mMenu, mNoteListSelected, false);
            CustomSharedPreferences.setSharedPreferencesDeleteMode(this, ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
        }
        else super.onBackPressed();
    }

    public void makeAlertDeleteMode() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.Theme_MaterialComponents_Light_Dialog_Alert)
                .setTitle("¿Quieres eliminar las notas seleccionadas ("+ totalCountSelected +")?")
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
                                    mTextViewListLLSelected.get(x),
                                    getApplicationContext(), R.color.colorPrimaryActionBar);
                        }
                        mMenu.getItem(0).setVisible(false);
                        mMenu.getItem(1).setVisible(false);
                        mMenu.getItem(2).setVisible(true);
                        mMenu.getItem(3).setVisible(true);
                        mMenu.getItem(4).setVisible(true);

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

    private void makeSnackBar() {
        final Snackbar snackbar = Snackbar.make(fm, "Notas eliminadas", Snackbar.LENGTH_LONG);
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
                for (int x = 0; x < mNoteListSelected.size(); x++) {
                    if (mNoteListSelected.get(x).getRecordatorio() == 1) {
                        WorkManager.getInstance(getApplicationContext()).cancelWorkById(UUID.fromString(mNoteListSelected.get(x).getTag()));
                    }
                }
                super.onDismissed(transientBottomBar, event);
                tb.setTranslationY(0);
                mNoteListSelected.clear();
                mTextViewListSelected.clear();
                mTextViewListLLSelected.clear();
                mNotesIdList.clear();
                isDeleteModeOpen = false;
                CustomSharedPreferences.setSharedPreferencesDeleteMode(getApplicationContext(), ID_DELETEMODE_BUNDLE, isDeleteModeOpen);
            }
        });

        View snack = snackbar.getView();
        TextView snackText = snack.findViewById(com.google.android.material.R.id.snackbar_action);
        snackText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }
}

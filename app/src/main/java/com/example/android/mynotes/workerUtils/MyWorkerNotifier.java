package com.example.android.mynotes.workerUtils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.android.mynotes.CreateNoteActivity;
import com.example.android.mynotes.ListMainActivity;
import com.example.android.mynotes.R;
import com.example.android.mynotes.database.DatabaseQueries;
import com.example.android.mynotes.database.NoteEntity;

import java.util.Date;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;
import androidx.work.Operation;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorkerNotifier extends Worker {

    public static final String KEY_WORKER_TITLE_NOTIFICATION = "ID_TITLE";
    public static final String KEY_WORKER_ID_NOTIFICATION = "ID_NOTA";
    public static final String KEY_WORKER_MODE_NOTIFICATION = "ID_CREATION_MODE";
    private final int ID_NOTIFICACION = 2352345;

    public static final String FINISHED = "notificaction_recibida";

    public MyWorkerNotifier(Context ctx, WorkerParameters params) { super(ctx, params); }

    @Override
    public Result doWork() {
        String title = getInputData().getString(KEY_WORKER_TITLE_NOTIFICATION);
        int id = getInputData().getInt(KEY_WORKER_ID_NOTIFICATION, 0);
        int isChecklist = getInputData().getInt(KEY_WORKER_MODE_NOTIFICATION, 0);

        createNotification(title, id, isChecklist);
        return Result.success();
    }

    private void createNotification(String titulo, int id, int isChecklist) {
        Context ctx = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "NOTIFICATION_CHANNEL",
                    "Prioridad",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(ctx, "notificacion")
                .setColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_note_menu)
                .setContentTitle(titulo)
                .setContentText("Vengo a recordarte de que tienes una nota muy importante.")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(goToNote(id, isChecklist))
                .setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        notificationManager.notify(ID_NOTIFICACION, builder.build());
    }

    private PendingIntent goToNote(int id, int isChecklist) {
        Intent i = new Intent(getApplicationContext(), CreateNoteActivity.class);
        i.putExtra(CreateNoteActivity.NOTE_ID, id);
        i.putExtra(CreateNoteActivity.UPDATE_NOTE, true);
        i.putExtra(FINISHED, true);

        if (isChecklist == 1) i.putExtra(CreateNoteActivity.ID_CREATION_MODE, CreateNoteActivity.CREATION_MODE_1);
        else if (isChecklist == 0) i.putExtra(CreateNoteActivity.ID_CREATION_MODE, CreateNoteActivity.CREATION_MODE_2);

        return PendingIntent.getActivity(getApplicationContext(),
                14351345,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

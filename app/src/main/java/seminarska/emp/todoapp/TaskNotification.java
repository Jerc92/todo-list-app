package seminarska.emp.todoapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jerne on 10. 01. 2017.
 */

public class TaskNotification extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

        Bundle extras = intent.getExtras();

        String notificationText = "The task \"" +extras.getString("taskInfo")+"\" is due in "+extras.getString("reminderTime")+'!';

        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        notificationBuilder.setContentTitle(context.getResources().getString(R.string.notification_title));
        notificationBuilder.setContentText(notificationText);
        notificationBuilder.setAutoCancel(true);

        Bundle intentExtras = new Bundle();
        intentExtras.putString("category", extras.getString("category"));

        Intent mainIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(mainIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(1000, PendingIntent.FLAG_ONE_SHOT, intentExtras);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1000, notificationBuilder.build());
    }
}

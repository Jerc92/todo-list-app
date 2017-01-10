package seminarska.emp.todoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by jerne on 10. 01. 2017.
 */

public class TaskNotification extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm worked", Toast.LENGTH_LONG).show();
    }
}

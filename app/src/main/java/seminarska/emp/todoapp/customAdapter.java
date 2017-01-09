package seminarska.emp.todoapp;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by StatiS on 09/01/2017.
 */

public class customAdapter extends SimpleCursorAdapter {

    DatabaseConnector database;
    public customAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, DatabaseConnector db) {
        super(context, layout, c, from, to, flags);
        database = db;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View inView = super.getView(position, convertView, parent);
        final CheckBox cbox = (CheckBox) inView.findViewById(R.id.task_complete);
        final TextView tView = (TextView) inView.findViewById(R.id.listView_item);
        cbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inView.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                inView.setAlpha(1);
                            }
                        });
                Log.d("za foro!!!!", String.valueOf(database.getTaskID((String) tView.getText())));
                database.deleteTask(database.getTaskID((String) tView.getText()));
                notifyDataSetChanged();
            }
        });
        return inView;
    }
}

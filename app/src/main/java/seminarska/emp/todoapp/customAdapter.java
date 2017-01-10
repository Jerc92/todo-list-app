package seminarska.emp.todoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
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
    SharedPreferences sharedPreferences;



    public customAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags, DatabaseConnector db) {
        super(context, layout, c, from, to, flags);
        database = db;
        sharedPreferences = context.getSharedPreferences("points", Context.MODE_PRIVATE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View inView = super.getView(position, convertView, parent);
        final CheckBox cbox = (CheckBox) inView.findViewById(R.id.task_complete);
        final TextView tView = (TextView) inView.findViewById(R.id.listView_item);
        cbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SetTaskPoints().execute(tView.getText().toString());
                inView.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                inView.setAlpha(1);
                            }
                        });
                Log.d("za foro!!!!", String.valueOf(database.getTaskID((String) tView.getText())));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new DeleteTask().execute(tView.getText().toString());
                    }
                }, 2000);
            }
        });
        return inView;
    }

    private class DeleteTask extends AsyncTask<String, Object, Cursor> {
        @Override
        protected Cursor doInBackground(String... params) {
            database.open();
            database.deleteTask(database.getTaskID(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            changeCursor(result);
            database.close();
        }
    }

    private class SetTaskPoints extends AsyncTask<String, Object, Cursor> {
        @Override
        protected Cursor doInBackground(String... params) {
            database.open();
            return database.getOneTask(database.getTaskID(params[0]));
        }

        @Override
        protected void onPostExecute(Cursor result) {
            result.moveToFirst();

            int points = result.getInt(result.getColumnIndex("points"));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("points", sharedPreferences.getInt("points", 0)+points);
            editor.commit();
        }
    }
}

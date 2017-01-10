package seminarska.emp.todoapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

//TODO add new textview for the categories

public class ViewTask extends AppCompatActivity {

    private long rowID;

    static final String[] months = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    static final String[] reminderStrings = {"1 hour", "2 hours", "6 hours", "12 hours", "1 day", "2 days", "1 week"};


    TextView categoryTextView;
    TextView infoTextView;
    TextView deadlineTextView;
    TextView reminderTextView;
    TextView pointsTextView;

    String categoryData;
    String infoData;
    long deadlineData;
    String reminderData;
    int pointsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activity);

        categoryTextView = (TextView) findViewById(R.id.category_label);
        infoTextView = (TextView) findViewById(R.id.info_label);
        deadlineTextView = (TextView) findViewById(R.id.deadline_label);
        reminderTextView = (TextView) findViewById(R.id.reminder_label);
        pointsTextView = (TextView) findViewById(R.id.points_label);

        final Bundle extras = getIntent().getExtras();

        rowID = extras.getLong("row_id");
    }

    @Override
    protected void onResume() {
        super.onResume();

        new LoadTask().execute(rowID);
    }

    private class LoadTask extends AsyncTask<Long, Object, Cursor> {
        DatabaseConnector db = new DatabaseConnector(ViewTask.this);

        @Override
        protected Cursor doInBackground(Long... params){
            db.open();
            return db.getOneTask(params[0]);
        }

        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            result.moveToFirst();

            int categoryIndex = result.getColumnIndex("category");
            int infoIndex = result.getColumnIndex("info");
            int deadlineIndex = result.getColumnIndex("deadline");
            int reminderIndex = result.getColumnIndex("reminders");
            int pointsIndex = result.getColumnIndex("points");

            categoryData = result.getString(categoryIndex);
            infoData = result.getString(infoIndex);
            deadlineData = Long.parseLong(result.getString(deadlineIndex));
            reminderData = result.getString(reminderIndex);
            pointsData = result.getInt(pointsIndex);

            pointsTextView.setText(pointsTextView.getText().toString()+"\n"+pointsData);

            categoryTextView.setText(categoryTextView.getText().toString()+categoryData);

            // sets the label for task info on the string from the database

            infoTextView.setText(infoTextView.getText().toString()+infoData);

            // transforms the string from the db into a Calendar class and sets the deadline label to the proper format

            Calendar deadlineCalendar = Calendar.getInstance();
            deadlineCalendar.setTimeInMillis(deadlineData);

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(deadlineTextView.getText().toString());

            String dateString = deadlineCalendar.get(Calendar.DAY_OF_MONTH)+". "
                                +months[deadlineCalendar.get(Calendar.MONTH)]+" "+ deadlineCalendar.get(Calendar.YEAR);
            String hourString;
            if(deadlineCalendar.get(Calendar.MINUTE) < 10) {
                hourString = deadlineCalendar.get(Calendar.HOUR_OF_DAY)+":0"+deadlineCalendar.get(Calendar.MINUTE);
            } else {
                hourString = deadlineCalendar.get(Calendar.HOUR_OF_DAY)+":"+deadlineCalendar.get(Calendar.MINUTE);
            }

            stringBuilder.append(dateString).append(", ").append(hourString);

            deadlineTextView.setText(stringBuilder.toString());



            // converts the reminder data from the delimiters, it's used to fix the reminder label

            String[] reminderArray = reminderData.split(",");

            stringBuilder.delete(0, stringBuilder.length());

            stringBuilder.append(reminderTextView.getText().toString());

            for (int i = 0; i < reminderArray.length; i++) {
                int currentIndex = Integer.parseInt(reminderArray[i].split(":")[1]);
                stringBuilder.append(reminderStrings[currentIndex]).append(", ");
            }

            stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length()-1);

            reminderTextView.setText(stringBuilder.toString());

            result.close();
            db.close();
        }
    }

    public void editTask(View view) {
        Intent editTask = new Intent(ViewTask.this, AddEditTask.class);

        editTask.putExtra("row_id", rowID);
        editTask.putExtra("category", categoryData);
        editTask.putExtra("info", infoData);
        editTask.putExtra("deadline", deadlineData);
        editTask.putExtra("reminders", reminderData);
        editTask.putExtra("points", pointsData);

        startActivity(editTask);

        finish();
    }

    public void deleteTask(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewTask.this);

        builder.setTitle("Delete task");
        builder.setMessage("Do you really want to delete this task?");

        builder.setNegativeButton(android.R.string.cancel, null);

        builder.setPositiveButton(android.R.string.ok, deleteListener);

        builder.show();
    }

    DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int button) {
            final DatabaseConnector db = new DatabaseConnector(ViewTask.this);

            AsyncTask<Long, Object, Object> deleteAsyncTask = new AsyncTask<Long, Object, Object>() {
                @Override
                protected Object doInBackground(Long... params) {
                    db.deleteTask(params[0]);
                    return null;
                }
                @Override
                public void onPostExecute(Object result) {
                    finish();
                }
            };

            deleteAsyncTask.execute(rowID);
        }
    };
}

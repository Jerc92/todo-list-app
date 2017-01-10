package seminarska.emp.todoapp;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

// TODO add new textView/Spinner for the category, depending on if the category is already selected
// TODO add AlarmManager for the reminders (maybe disable reminders for recurring tasks?)

public class AddEditTask extends AppCompatActivity {

    static final long hourInMiliseconds = 3600000;

    static final String[] months = {"January", "February", "March", "April", "May", "June", "July",
                                    "August", "September", "October", "November", "December"};

    static final String[] reminderStrings = {"1 hour", "2 hours", "6 hours", "12 hours", "1 day", "2 days", "1 week"};

    boolean[] isReminderChecked = {false, false, false, false, false, false, false};

    long rowID;

    Calendar deadlineCalendar;
    String reminderTimes;
    String category = "others";
    int points;

    SharedPreferences sharedPreferences;

    TextView categoryTextView;
    EditText infoEditText;
    TextView deadlineTextView;
    TextView reminderTextView;
    Spinner pointsSpinner;
    ArrayAdapter<CharSequence> adapter;

    Dialog reminderDialog;
    Dialog datePickerDialog;
    Dialog timePickerDialog;

    DatePicker datePicker;
    TimePicker timePicker;

    Button timePickerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        categoryTextView = (TextView) findViewById(R.id.category_label);
        infoEditText = (EditText) findViewById(R.id.info_editText);
        reminderTextView = (TextView) findViewById(R.id.reminder_label);
        deadlineTextView = (TextView) findViewById(R.id.deadline_label);
        pointsSpinner = (Spinner) findViewById(R.id.points_spinner);

        deadlineCalendar = Calendar.getInstance();

        sharedPreferences = getSharedPreferences("notifications", Context.MODE_PRIVATE);

        reminderDialog = new Dialog(AddEditTask.this);
        reminderDialog.setContentView(R.layout.checkbox_template);
        setReminderDialog();

        datePickerDialog = new Dialog(AddEditTask.this);
        datePickerDialog.setContentView(R.layout.date_picker);
        datePicker = (DatePicker) datePickerDialog.findViewById(R.id.datePicker);

        // Set minDate to current date

        timePickerDialog = new Dialog(AddEditTask.this);
        timePickerDialog.setContentView(R.layout.time_picker);
        timePicker = (TimePicker) timePickerDialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.points_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        pointsSpinner.setAdapter(adapter);

        pointsSpinner.setOnItemSelectedListener(spinnerItemSelected);

        points = Integer.parseInt(adapter.getItem(0).toString());

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            if(extras.containsKey("category")) {
                categoryTextView.setText(categoryTextView.getText().toString()+extras.getString("category"));
                category = extras.getString("category");
            }
            if (extras.containsKey("row_id")) {
                handleExistingTask(extras);
            }
        }

        // Set minTime to current time, if date == current date
    }

    public void showReminders(View view) {
        reminderDialog.show();
    }

    public void setReminderDialog() {
        ((CheckBox) reminderDialog.findViewById(R.id.first_checkbox)).setText(reminderStrings[0]);
        ((CheckBox) reminderDialog.findViewById(R.id.second_checkbox)).setText(reminderStrings[1]);
        ((CheckBox) reminderDialog.findViewById(R.id.third_checkbox)).setText(reminderStrings[2]);
        ((CheckBox) reminderDialog.findViewById(R.id.fourth_checkbox)).setText(reminderStrings[3]);
        ((CheckBox) reminderDialog.findViewById(R.id.fifth_checkbox)).setText(reminderStrings[4]);
        ((CheckBox) reminderDialog.findViewById(R.id.sixth_checkbox)).setText(reminderStrings[5]);
        ((CheckBox) reminderDialog.findViewById(R.id.seventh_checkbox)).setText(reminderStrings[6]);
        Button button = (Button) reminderDialog.findViewById(R.id.checkbox_button);
        button.setText(getString(R.string.confirm_reminders));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminderConfirm(v);
            }
        });
    }

    public void reminderConfirm(View view) {
        LinearLayout linearLayout = (LinearLayout) reminderDialog.findViewById(R.id.week_linear);
        CheckBox checkBox;
        String temp;
        StringBuilder reminderText = new StringBuilder();
        reminderText.append("Reminders:\n");
        for (int i = 0; i < 7; i++) {
            checkBox = (CheckBox) linearLayout.getChildAt(i);
            if (checkBox.isChecked()) {
                temp = checkBox.getText().toString();
                isReminderChecked[i] = !isReminderChecked[i];
                reminderText.append(temp).append(", ");
            }
        }
        reminderText.delete(reminderText.length()-2, reminderText.length()-1);

        reminderTextView.setText(reminderText.toString());

        reminderDialog.dismiss();
    }

    public void setDeadline(View view) {
        datePickerDialog.show();

        timePickerButton = (Button) timePickerDialog.findViewById(R.id.time_button);

        timePickerButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                deadlineCalendar.set(Calendar.YEAR, datePicker.getYear());
                deadlineCalendar.set(Calendar.MONTH, datePicker.getMonth());
                deadlineCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                deadlineCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                deadlineCalendar.set(Calendar.MINUTE, timePicker.getMinute());
                deadlineCalendar.set(Calendar.SECOND, 0);

                StringBuilder deadlineString = new StringBuilder();

                deadlineString.append("Deadline:\n");

                String dateString = datePicker.getDayOfMonth()+". "+months[datePicker.getMonth()]+" "+ datePicker.getYear();
                String hourString;
                if(timePicker.getMinute() < 10) {
                    hourString = timePicker.getHour()+":0"+timePicker.getMinute();
                } else {
                    hourString = timePicker.getHour()+":"+timePicker.getMinute();
                }

                deadlineString.append(dateString).append(", ").append(hourString);

                deadlineTextView.setText(deadlineString.toString());

                timePickerDialog.dismiss();
                datePickerDialog.dismiss();
            }
        });
    }

    public void confirmDate(View view) {
        timePickerDialog.show();
    }

    // Adds the time in miliseconds for all selected reminders to the
    // string that is used to store in the database

    public void addRemindersToString() {
        // adds the time in miliseconds as well as the index of that reminder for later use
        // add the pendingintent id instead of the time and set the pendingintent

        long temp;

        StringBuilder sb = new StringBuilder();
        if (isReminderChecked[0]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(0).append(",");
        }
        if (isReminderChecked[1]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *2;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 1);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(1).append(",");
        }
        if (isReminderChecked[2]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *6;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 2);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(2).append(",");
        }
        if (isReminderChecked[3]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *12;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 3);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(3).append(",");
        }
        if (isReminderChecked[4]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 4);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(4).append(",");
        }
        if (isReminderChecked[5]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24*2;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 5);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(5).append(",");
        }
        if (isReminderChecked[6]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24*7;
            int code = sharedPreferences.getInt("notifications", 0);
            createReminder(code, temp, 6);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("notifications", sharedPreferences.getInt("notifications", 0)+1);
            editor.apply();
            sb.append(code).append(":").append(6).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        reminderTimes = sb.toString();
    }

    public void createReminder(int code, long time, int stringIndex) {
        Intent intent = new Intent(this, TaskNotification.class);
        intent.putExtra("reminderTime", reminderStrings[stringIndex]);
        intent.putExtra("taskInfo", infoEditText.getText().toString());
        intent.putExtra("category", category);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), code, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.set(AlarmManager.RTC, time, pendingIntent);
    }

    public void cancelReminder(int code) {
        Intent intent = new Intent(this, TaskNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), code, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            pendingIntent.cancel();
        }
    }

    public void handleExistingTask(Bundle extras) {
        rowID = extras.getLong("row_id");

        infoEditText.setText(infoEditText.getText().toString()+extras.getString("info"));

        deadlineCalendar.setTimeInMillis(extras.getLong("deadline"));

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

        datePicker.updateDate(deadlineCalendar.get(Calendar.YEAR), deadlineCalendar.get(Calendar.MONTH), deadlineCalendar.get(Calendar.DAY_OF_MONTH));
        timePicker.setMinute(deadlineCalendar.get(Calendar.MINUTE));
        timePicker.setHour(deadlineCalendar.get(Calendar.HOUR_OF_DAY));

        String[] reminderArray = extras.getString("reminders").split(",");

        stringBuilder.delete(0, stringBuilder.length());

        stringBuilder.append(reminderTextView.getText().toString());

        for (int i = 0; i < reminderArray.length; i++) {
            int currentIndex = Integer.parseInt(reminderArray[i].split(":")[1]);
            cancelReminder(Integer.parseInt(reminderArray[i].split(":")[0]));
            stringBuilder.append(reminderStrings[currentIndex]).append(", ");
            isReminderChecked[currentIndex] = true;
        }

        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length()-1);

        reminderTextView.setText(stringBuilder.toString());

        LinearLayout linearLayout = (LinearLayout) reminderDialog.findViewById(R.id.week_linear);
        CheckBox checkBox;

        for (int i = 0; i < isReminderChecked.length; i++) {
            if (isReminderChecked[i]) {
                checkBox = (CheckBox) linearLayout.getChildAt(i);
                checkBox.setChecked(true);
            }
        }

        int position = adapter.getPosition(String.valueOf(extras.getInt("points")));
        pointsSpinner.setSelection(position);
    }

    public void saveTask(View view) {
        if (infoEditText.getText().length() != 0) {

            addRemindersToString();

            AsyncTask<Object, Object, Object> saveTaskAsync = new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {
                    saveTaskToDB();
                    return null;
                }
                @Override
                protected void onPostExecute(Object result) {
                    finish();
                }
            };

            saveTaskAsync.execute((Object[]) null);

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddEditTask.this);

            builder.setTitle("Emtpy fields!");
            builder.setMessage("The task info field can't be emtpy!");
            builder.setPositiveButton("Ok", null);

            builder.show();
        }
    }

    private void saveTaskToDB() {
        DatabaseConnector db = new DatabaseConnector(this);

        if(getIntent().getExtras() == null || !getIntent().getExtras().containsKey("row_id")) {

            db.insertTask(category, infoEditText.getText().toString(), reminderTimes, Long.toString(deadlineCalendar.getTimeInMillis()), points);

        } else {

            db.updateTask(rowID, category, infoEditText.getText().toString(), reminderTimes, Long.toString(deadlineCalendar.getTimeInMillis()), points);

        }
    }

    AdapterView.OnItemSelectedListener spinnerItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            points = Integer.parseInt(adapter.getItem(position).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}

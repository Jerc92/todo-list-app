package seminarska.emp.todoapp;

import android.app.Dialog;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class AddTask extends AppCompatActivity {

    static final long hourInMiliseconds = 3600000;

    static final String[] months = {"January", "February", "March", "April", "May", "June", "July",
                                    "August", "September", "October", "November", "December"};

    static final String[] reminderStrings = {"1 hour", "2 hours", "6 hours", "12 hours", "1 day", "2 days", "1 week"};

    boolean[] isReminderChecked = {false, false, false, false, false, false, false};

    long rowID;

    Calendar deadlineCalendar;
    String reminderTimes;

    EditText info;
    TextView deadlineLabel;
    TextView reminderLabel;

    Dialog reminders;
    Dialog datePickerDialog;
    Dialog timePickerDialog;

    DatePicker datePicker;
    TimePicker timePicker;

    Button timePickerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        info = (EditText) findViewById(R.id.info_editText);
        reminderLabel = (TextView) findViewById(R.id.reminder_label);
        deadlineLabel = (TextView) findViewById(R.id.deadline_label);

        Bundle extras = getIntent().getExtras();

        deadlineCalendar = Calendar.getInstance();

        reminders = new Dialog(AddTask.this);
        reminders.setContentView(R.layout.checkbox_template);
        setReminderDialog();

        datePickerDialog = new Dialog(AddTask.this);
        datePickerDialog.setContentView(R.layout.date_picker);
        datePicker = (DatePicker) datePickerDialog.findViewById(R.id.datePicker);

        // Set minDate to current date

        timePickerDialog = new Dialog(AddTask.this);
        timePickerDialog.setContentView(R.layout.time_picker);
        timePicker = (TimePicker) timePickerDialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        // Set minTime to current time, if date == current date
    }

    public void showReminders(View view) {
        reminders.show();
    }

    public void setReminderDialog() {
        ((CheckBox) reminders.findViewById(R.id.first_checkbox)).setText(reminderStrings[0]);
        ((CheckBox) reminders.findViewById(R.id.second_checkbox)).setText(reminderStrings[1]);
        ((CheckBox) reminders.findViewById(R.id.third_checkbox)).setText(reminderStrings[2]);
        ((CheckBox) reminders.findViewById(R.id.fourth_checkbox)).setText(reminderStrings[3]);
        ((CheckBox) reminders.findViewById(R.id.fifth_checkbox)).setText(reminderStrings[4]);
        ((CheckBox) reminders.findViewById(R.id.sixth_checkbox)).setText(reminderStrings[5]);
        ((CheckBox) reminders.findViewById(R.id.seventh_checkbox)).setText(reminderStrings[6]);
        Button button = (Button) reminders.findViewById(R.id.checkbox_button);
        button.setText(getString(R.string.confirm_reminders));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminderConfirm(v);
            }
        });
    }

    public void reminderConfirm(View view) {
        LinearLayout linearLayout = (LinearLayout) reminders.findViewById(R.id.week_linear);
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

        reminderLabel.setText(reminderText.toString());

        reminders.dismiss();
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

                deadlineLabel.setText(deadlineString.toString());

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
        long temp;
        StringBuilder sb = new StringBuilder();
        if (isReminderChecked[0]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds;
            sb.append(temp).append(",");
        }
        if (isReminderChecked[1]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *2;
            sb.append(temp).append(",");
        }
        if (isReminderChecked[2]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *6;
            sb.append(temp).append(",");
        }
        if (isReminderChecked[3]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *12;
            sb.append(temp).append(",");
        }
        if (isReminderChecked[4]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24;
            sb.append(temp).append(",");
        }
        if (isReminderChecked[5]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24*2;
            sb.append(temp).append(",");
        }
        if (isReminderChecked[6]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24*7;
            sb.append(temp).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        reminderTimes = sb.toString();
    }

    public void saveTask(View view) {
        if (info.getText().length() != 0) {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AddTask.this);

            builder.setTitle("Emtpy fields!");
            builder.setMessage("The task info field can't be emtpy!");
            builder.setPositiveButton("Ok", null);

            builder.show();
        }
    }

    private void saveTaskToDB() {
        DatabaseConnector db = new DatabaseConnector(this);

        if(getIntent().getExtras() == null) {

            db.insertTask(0, info.getText().toString(), reminderTimes, Long.toString(deadlineCalendar.getTimeInMillis()));

        } else {

        }
    }
}

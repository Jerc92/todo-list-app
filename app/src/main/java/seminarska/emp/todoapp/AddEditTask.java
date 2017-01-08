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

    TextView categoryTextView;
    EditText infoEditText;
    TextView deadlineTextView;
    TextView reminderTextView;

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

        deadlineCalendar = Calendar.getInstance();

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

        Bundle extras = getIntent().getExtras();

        if(extras.containsKey("category")) {
            categoryTextView.setText(categoryTextView.getText().toString()+extras.getString("category"));
            category = extras.getString("category");
        }
        if (extras.containsKey("row_id")) {
            handleExistingTask(extras);
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

        long temp;
        StringBuilder sb = new StringBuilder();
        if (isReminderChecked[0]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds;
            sb.append(temp).append(":").append(0).append(",");
        }
        if (isReminderChecked[1]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *2;
            sb.append(temp).append(":").append(1).append(",");
        }
        if (isReminderChecked[2]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *6;
            sb.append(temp).append(":").append(2).append(",");
        }
        if (isReminderChecked[3]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *12;
            sb.append(temp).append(":").append(3).append(",");
        }
        if (isReminderChecked[4]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24;
            sb.append(temp).append(":").append(4).append(",");
        }
        if (isReminderChecked[5]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24*2;
            sb.append(temp).append(":").append(5).append(",");
        }
        if (isReminderChecked[6]) {
            temp = deadlineCalendar.getTimeInMillis();
            temp -= hourInMiliseconds *24*7;
            sb.append(temp).append(":").append(6).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        reminderTimes = sb.toString();
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

        if(getIntent().getExtras() == null) {

            db.insertTask(category, infoEditText.getText().toString(), reminderTimes, Long.toString(deadlineCalendar.getTimeInMillis()));

        } else {

            db.updateTask(rowID, category, infoEditText.getText().toString(), reminderTimes, Long.toString(deadlineCalendar.getTimeInMillis()));

        }
    }
}

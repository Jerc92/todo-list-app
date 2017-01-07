package seminarska.emp.todoapp;

import android.app.Dialog;
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

import java.util.ArrayList;
import java.util.Calendar;

public class AddTask extends AppCompatActivity {

    static final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    static final String[] months = {"January", "February", "March", "April", "May", "June", "July",
                                    "August", "September", "October", "November", "December"};

    static final String[] reminderStrings = {"1 hour", "2 hours", "6 hours", "12 hours", "1 day", "2 days", "1 week"};

    boolean[] isReminderChecked = {false, false, false, false, false, false, false};

    ArrayList<String> repeatedDays;
    Calendar deadlineCalendar;

    EditText info;
    TextView recurringDays;
    TextView deadlineLabel;
    TextView reminderLabel;

    Dialog weekDays;
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
        recurringDays = (TextView) findViewById(R.id.repeat_label);
        reminderLabel = (TextView) findViewById(R.id.reminder_label);
        deadlineLabel = (TextView) findViewById(R.id.deadline_label);

        deadlineCalendar = Calendar.getInstance();

        weekDays = new Dialog(AddTask.this);
        weekDays.setContentView(R.layout.checkbox_template);
        setDaysDialog();

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

    public void showDays(View view) {
        weekDays.show();
    }

    public void showReminders(View view) {
        reminders.show();
    }

    public void confirmDate(View view) {
        timePickerDialog.show();
    }

    public void setDaysDialog() {
        ((CheckBox) weekDays.findViewById(R.id.first_checkbox)).setText(days[0]);
        ((CheckBox) weekDays.findViewById(R.id.second_checkbox)).setText(days[1]);
        ((CheckBox) weekDays.findViewById(R.id.third_checkbox)).setText(days[2]);
        ((CheckBox) weekDays.findViewById(R.id.fourth_checkbox)).setText(days[3]);
        ((CheckBox) weekDays.findViewById(R.id.fifth_checkbox)).setText(days[4]);
        ((CheckBox) weekDays.findViewById(R.id.sixth_checkbox)).setText(days[5]);
        ((CheckBox) weekDays.findViewById(R.id.seventh_checkbox)).setText(days[6]);
        Button button = (Button) weekDays.findViewById(R.id.checkbox_button);
        button.setText(getString(R.string.confirm_days));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatConfirm(v);
            }
        });
    }

    public void repeatConfirm(View view) {
        repeatedDays = new ArrayList<>();
        LinearLayout linearLayout = (LinearLayout) weekDays.findViewById(R.id.week_linear);
        CheckBox checkBox;
        String temp;
        StringBuilder recurringDaysText = new StringBuilder();
        recurringDaysText.append("Recurring days:\n");
        for (int i = 0; i < 7; i++) {
            checkBox = (CheckBox) linearLayout.getChildAt(i);
            if (checkBox.isChecked()) {
                temp = checkBox.getText().toString();
                repeatedDays.add(temp);
                recurringDaysText.append(temp.substring(0, 3)).append(", ");
            }
        }
        recurringDaysText.delete(recurringDaysText.length()-2, recurringDaysText.length()-1);

        recurringDays.setText(recurringDaysText.toString());

        weekDays.dismiss();
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
}

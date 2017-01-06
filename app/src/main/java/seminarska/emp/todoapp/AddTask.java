package seminarska.emp.todoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class AddTask extends AppCompatActivity {

    EditText info;
    Spinner recurringDays;
    String[] DayOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        info = (EditText) findViewById(R.id.info_editText);
        recurringDays = (Spinner) findViewById(R.id.repeat_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.weekday, R.id.day_checkbox, DayOfWeek);

        recurringDays.setAdapter(adapter);
    }
}

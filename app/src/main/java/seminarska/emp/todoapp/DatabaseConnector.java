package seminarska.emp.todoapp;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jerne on 4. 01. 2017.
 */

public class DatabaseConnector extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ListDatabase";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase database;

    public DatabaseConnector(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open() throws SQLException {
        database = this.getWritableDatabase();
    }


    public void close() {
        if (database != null) database.close(); // close the database connection
    }

    // v repeatingDays in reminder se shranijo dnevi/čas v text obliki, ki se pretvori pozneje v Date format

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String createQueryTasks = "CREATE TABLE tasks" +
                "(_id integer primary key autoincrement, " +
                "category TEXT, " +
                "info TEXT, " +
                "repeatingDays TEXT, " +
                "reminders TEXT, " +
                "deadline TEXT);";

        db.execSQL(createQueryTasks);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

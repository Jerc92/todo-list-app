package seminarska.emp.todoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;

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
        Log.d("DatabaseConnector", "onCreate called");
        //two tables actually might be simpler
        //daily switch might make more sense for categories than individual tasks
        final String createQueryTasks = "CREATE TABLE tasks" +
                "(_id integer primary key autoincrement, " +
                "category INT, " +
                "info TEXT, " +
                "reminders TEXT, " +
                "deadline TEXT);";
        final String createQueryCategories = "CREATE TABLE categories" +
                "(_id integer primary key autoincrement, " +
                "category TEXT, " +
                "repeats INT);";
        db.execSQL(createQueryTasks);
        db.execSQL(createQueryCategories);

        //add default categories
        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //clean database, has to be changed when app is finished
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS categories");
        onCreate(db);
    }

    public void insertTask(int category, String info, String reminders, String deadline) {
        final ContentValues newTask = new ContentValues();
        newTask.put("category", category);
        newTask.put("info", info);
        newTask.put("reminders", reminders);
        newTask.put("deadline", deadline);

        open();
        database.insert("tasks", null, newTask);
        close();
    }

    public Cursor getAllTasks() {
        return database.query("tasks", new String[]{"_id", "info"},
                            null, null, null, null, "_id");
    }

    public Cursor getTasksByCategory(String category) {
        return database.query("tasks", new String[]{"_id", "info"},
                            "category="+category, null, null, null, null);
    }

    public Cursor getOneTask(long id) {
        return database.query("tasks", null, "_id="+id, null, null, null, null);
    }

    public void updateTask(long id, int category, String info, String reminders, String deadline) {
        final ContentValues editTask = new ContentValues();

        editTask.put("category", category);
        editTask.put("info", info);
        editTask.put("reminders", reminders);
        editTask.put("deadline", deadline);

        open();
        database.update("tasks", editTask, "_id=" + id, null);
        close();
    }

    public void deleteTask(long id) {
        open();
        database.delete("tasks", "_id"+id, null);
        close();
    }

    public Cursor getCategories() {
        //getting writable database everytime might not be the best idea..
        database = getWritableDatabase();
        return database.rawQuery("SELECT category FROM categories", null);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        //temporary way to add default categories
        //CHANGE WHEN APP IS FINISHED
        String insertQuery = "INSERT INTO categories VALUES (NULL, 'Daily', 1);";
        String insertQuery2 = "INSERT INTO categories VALUES (NULL, 'Personal', 0);";
        String insertQuery3 = "INSERT INTO categories VALUES (NULL, 'Work', 0);";
        db.execSQL(insertQuery);
        db.execSQL(insertQuery2);
        db.execSQL(insertQuery3);
    }
}

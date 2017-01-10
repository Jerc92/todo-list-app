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

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseConnector", "onCreate called");
        //two tables actually might be simpler
        //daily switch might make more sense for categories than individual tasks
        final String createQueryTasks = "CREATE TABLE tasks" +
                "(_id integer primary key autoincrement, " +
                "category TEXT, " +
                "info TEXT, " +
                "reminders TEXT, " +
                "deadline TEXT, " +
                "points int);";
        final String createQueryCategories = "CREATE TABLE categories" +
                "(_id integer primary key autoincrement, " +
                "category TEXT, " +
                "daily INT);";
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

    public void insertTask(String category, String info, String reminders, String deadline, int points) {
        final ContentValues newTask = new ContentValues();
        newTask.put("category", category);
        newTask.put("info", info);
        newTask.put("reminders", reminders);
        newTask.put("deadline", deadline);
        newTask.put("points", points);

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
                            "category='"+category+"'", null, null, null, null);
    }

    public Cursor getOneTask(long id) {
        return database.query("tasks", null, "_id="+id, null, null, null, null);
    }

    public void updateTask(long id, String category, String info, String reminders, String deadline, int points) {
        final ContentValues editTask = new ContentValues();

        editTask.put("category", category);
        editTask.put("info", info);
        editTask.put("reminders", reminders);
        editTask.put("deadline", deadline);
        editTask.put("points", points);

        open();
        database.update("tasks", editTask, "_id=" + id, null);
        close();
    }

    public void deleteTask(long id) {
        open();
        database.delete("tasks", "_id="+id, null);
        close();
    }


    public int getTaskID(String name) {
        database = getWritableDatabase();
        Cursor c = database.rawQuery("SELECT _id FROM tasks WHERE info = '" + name +"'", null);
        c.moveToFirst();
        return c.getInt(0);
    }

    public boolean addCategory(String name, boolean daily) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int isDaily = (daily) ? 1 : 0;
        contentValues.put("category", name);
        contentValues.put("daily", isDaily);

        //checking success or failure of function call, insert returns -1 on failure
        return (db.insert("categories", null, contentValues) != -1);
    }

    public void deleteCategory(String category) {
        open();
        database.delete("categories", "category='"+ category +"'", null);
        close();
    }

    public Cursor getCategories() {
        //getting writable database everytime might not be the best idea..
        database = getWritableDatabase();
        return database.rawQuery("SELECT category FROM categories WHERE category != 'others'", null);
    }

    public int getCategoryID(String name) {
        database = getWritableDatabase();
        Cursor c = database.rawQuery("SELECT _id FROM categories WHERE category = '" + name +"'", null);
        c.moveToFirst();
        return c.getInt(0);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        //temporary way to add default categories
        //CHANGE WHEN APP IS FINISHED
        String insertQuery0 = "INSERT INTO categories VALUES (0, 'others', 0);";
        String insertQuery = "INSERT INTO categories VALUES (NULL, 'Daily', 1);";
        String insertQuery2 = "INSERT INTO categories VALUES (NULL, 'Personal', 0);";
        String insertQuery3 = "INSERT INTO categories VALUES (NULL, 'Work', 0);";
        db.execSQL(insertQuery0);
        db.execSQL(insertQuery);
        db.execSQL(insertQuery2);
        db.execSQL(insertQuery3);
    }
}

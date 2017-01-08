package seminarska.emp.todoapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView pointsText;
    SharedPreferences sharedPreferences;

    NavigationView navigationView;
    SubMenu navViewSubMenu;

    ListView taskListView;

    DatabaseConnector db;
    CursorAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        taskListView = (ListView) findViewById(R.id.task_list);
        taskListView.setOnItemClickListener(viewTaskListener);

        //onUpgrade called every launch to truncate db for easier debugging
        //CHANGE WHEN APP IS FINISHED
        db = new DatabaseConnector(this);
        db.onUpgrade(db.getWritableDatabase(), 1, 2);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigation views submenu needed to be populated from db in populateNavMenu method
        navViewSubMenu = navigationView.getMenu().findItem(R.id.categoriesSubMenu).getSubMenu();
        populateNavMenu(navViewSubMenu);

        sharedPreferences = getSharedPreferences("points", Context.MODE_PRIVATE);

        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("points")) {
                    pointsText.setText(sharedPreferences.getInt(key, 0)+"");
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        View headerView = navigationView.getHeaderView(0);

        pointsText = (TextView) headerView.findViewById(R.id.points);

        pointsText.setText(sharedPreferences.getInt("points", 0)+"");

        taskAdapter = new SimpleCursorAdapter(this, R.layout.task_list_item, null, new String[]{"info"}, new int[]{R.id.listView_item}, 0);

        taskListView.setAdapter(taskAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();

        new GetTasks().execute();
    }

    @Override
    public void onStop() {
        taskAdapter.changeCursor(null);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetTasks extends AsyncTask<Object, Object, Cursor> {
        @Override
        protected Cursor doInBackground(Object... params) {
            db.open();
            return db.getAllTasks();
        }

        @Override
        protected void onPostExecute(Cursor result){
            taskAdapter.changeCursor(result);
            db.close();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //dynamic category item clicks could be handled by item.getTitle()
        Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();

        if (id == R.id.nav_rewards) {
        } else if (id == R.id.nav_all) {

        } else if (id == R.id.nav_addNew) {
            addCategoryPrompt();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createTask(View view) {
        Intent intent = new Intent(MainActivity.this, AddEditTask.class);
        startActivity(intent);
    }

    public void addCategoryPrompt () {
        //create alert prompt and add new category to database when ok button is pressed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New category");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.new_category_prompt, (ViewGroup) findViewById(R.id.content_main_linear), false);
        // Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.newCategoryInput);
        final CheckBox checkBox = (CheckBox) viewInflated.findViewById(R.id.newCategoryDaily);
        builder.setView(viewInflated);
        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String name = input.getText().toString();
                db.addCategory(name, checkBox.isChecked());
                navViewSubMenu.add(R.id.categoriesGroup, db.getCategoryID(name), Menu.NONE, name).setIcon(R.drawable.ic_label_outline_black_24dp);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void populateNavMenu(SubMenu navViewSubMenu) {
        //should make general addNavMenuItem function later, to be called at startup and when new categories are made by user
        Cursor categories = db.getCategories();
        categories.moveToFirst();
        for (int i = 0; i < categories.getCount(); i++) {
            //create menu entries from categories cursor data
            navViewSubMenu.add(R.id.categoriesGroup, db.getCategoryID(categories.getString(0)), Menu.NONE, categories.getString(0)).setIcon(R.drawable.ic_label_outline_black_24dp);
            categories.moveToNext();
        }
        categories.close();
    }

    AdapterView.OnItemClickListener viewTaskListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Intent viewTask = new Intent(MainActivity.this, ViewTask.class);

            viewTask.putExtra("row_id", id);
            startActivity(viewTask);
        }
    };
}

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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// TODO add category to extras, so it populates the right list, add global variable currentCategory which changes once you switch on the list


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView pointsText;
    SharedPreferences sharedPreferences;
    String currentCategory = "others";
    int currentIsRepeatable = 0;

    NavigationView navigationView;
    SubMenu navViewSubMenu;
    DrawerLayout drawer;


    ListView taskListView;

    DatabaseConnector db;
    customAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        drawer.addDrawerListener(changePoints);

        taskListView = (ListView) findViewById(R.id.task_list);
        taskListView.setOnItemClickListener(viewTaskListener);

        //onUpgrade called every launch to truncate db for easier debugging
        //CHANGE WHEN APP IS FINISHED
        //db = new DatabaseConnector(this);
        //db.onUpgrade(db.getWritableDatabase(), 1, 2);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigation views submenu needed to be populated from db in populateNavMenu method
        navViewSubMenu = navigationView.getMenu().findItem(R.id.categoriesSubMenu).getSubMenu();
        populateNavMenu(navViewSubMenu);

        sharedPreferences = getSharedPreferences("points", Context.MODE_PRIVATE);

        View headerView = navigationView.getHeaderView(0);

        pointsText = (TextView) headerView.findViewById(R.id.points);

        pointsText.setText(sharedPreferences.getInt("points", 0)+"");
        //this, R.layout.task_list_item, null, new String[]{"info"}, new int[]{R.id.listView_item}, 0
        taskAdapter = new customAdapter(this, R.layout.task_list_item, null, new String[]{"info"}, new int[]{R.id.listView_item}, 0,  db);

        taskListView.setAdapter(taskAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();

        new GetTasks().execute(currentCategory);
    }

    @Override
    public void onStop() {
        taskAdapter.changeCursor(null);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        //hide or show categoryDelete menuItem depending if current category is all
        menu.findItem(R.id.action_categoryDelete).setVisible(!currentCategory.equals("others"));
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
        } else if (id == R.id.action_categoryDelete) {
            navViewSubMenu.removeItem(db.getCategoryID(currentCategory));
            db.deleteCategory(currentCategory);
            currentCategory = "others";
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetTasks extends AsyncTask<String, Object, Cursor> {
        @Override
        protected Cursor doInBackground(String... params) {
            db.open();
            if (!params[0].equals("others")) {
                return db.getTasksByCategory(params[0]);
            } else {
                return db.getAllTasks();
            }
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
        if (id == R.id.nav_rewards) {

        } else if (id == R.id.nav_all) {
            currentCategory = "others";
            currentIsRepeatable = db.getCategoryIsRepeatable(currentCategory);
            new GetTasks().execute(currentCategory);
        } else if (id == R.id.nav_addNew) {
            addCategoryPrompt();
        } else {
            currentCategory = item.getTitle().toString();
            currentIsRepeatable = db.getCategoryIsRepeatable(currentCategory);
            new GetTasks().execute(currentCategory);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createTask(View view) {
        Intent intent = new Intent(MainActivity.this, AddEditTask.class);
        if (!currentCategory.equals("others")) {
            intent.putExtra("category", currentCategory);
        }
        intent.putExtra("isRepeatable", currentIsRepeatable);
        startActivity(intent);
    }

    public void addCategoryPrompt () {
        //create alert prompt and add new category to database when ok button is pressed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New category");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.new_category_prompt,
                (ViewGroup) findViewById(R.id.content_main_linear), false);
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
                addNavMenuItem(navViewSubMenu, name);
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
        //gets categories from db, calls addNavMenuItem() for each of them
        Cursor categories = db.getCategories();
        categories.moveToFirst();
        do {
            addNavMenuItem(navViewSubMenu, categories.getString(0));
        } while (categories.moveToNext());
        //set All to checked for startup
        navigationView.setCheckedItem(navViewSubMenu.findItem(R.id.nav_all).getItemId());
        categories.close();
    }
    public void addNavMenuItem(SubMenu navViewSubMenu, String name) {
        //adds items to NavMenu and sets onLongClickListeners for them
        navViewSubMenu.add(R.id.categoriesGroup, db.getCategoryID(name),
                Menu.NONE, name).setIcon(R.drawable.ic_label_outline_black_24dp).setCheckable(true);
    }
    AdapterView.OnItemClickListener viewTaskListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Intent viewTask = new Intent(MainActivity.this, ViewTask.class);

            viewTask.putExtra("row_id", id);
            viewTask.putExtra("category", currentCategory);
            viewTask.putExtra("isRepeatable", currentIsRepeatable);
            startActivity(viewTask);
        }
    };

    DrawerLayout.DrawerListener changePoints = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            pointsText.setText(sharedPreferences.getInt("points", 0)+"");
        }

        @Override
        public void onDrawerOpened(View drawerView) {}

        @Override
        public void onDrawerClosed(View drawerView) {}

        @Override
        public void onDrawerStateChanged(int newState) {}
    };
}
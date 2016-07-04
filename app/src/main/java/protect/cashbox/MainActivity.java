package protect.cashbox;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import protect.cashbox.account.AccountListActivity;
import protect.cashbox.category.Category;
import protect.cashbox.category.CategoryListActivity;
import protect.cashbox.impex.ExportTask;
import protect.cashbox.impex.ImportTask;
import protect.cashbox.transaction.ExpenseListActivity;
import protect.cashbox.transaction.RevenueListActivity;
import protect.cashbox.transaction.TransactionDetailActivity;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.Mode;
import protect.cashbox.util.ProgressTask;
import protect.cashbox.util.Util;

public class MainActivity extends CashboxActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AsyncTask<Void, Void, Void> impexTask;
    private long monthStart;
    private long monthEnd;
    private TextView dateRange;
    private DateFormat formatter;
    private Calendar calendar;

    private DashboardCategoryStatsAdapter categoryListAdapter;
    private ListView categoryList;
    private TextView categoryListNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_drawer);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //INIT FAB
        findViewById(R.id.main_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), TransactionDetailActivity.class);
                final Bundle b = new Bundle();
                b.putString(Constants.EXTRAS_VIEW_MODE, Mode.ADD.toString());
                b.putInt(Constants.EXTRAS_TRANSACTION_TYPE, DatabaseManager.TransactionTable.EXPENSE);
                i.putExtras(b);
                startActivity(i);
            }
        });

        //INIT DRAWER
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ((NavigationView) findViewById(R.id.main_nav)).setNavigationItemSelectedListener(this);

        //INIT FILTERING DATE INTERVAL
        calendar = Calendar.getInstance();
        dateRange = (TextView) findViewById(R.id.dateRange);
        formatter = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        monthStart = Util.getMonthStart(calendar);
        monthEnd = Util.getMonthEnd(calendar);
        dateRange.setText(formatter.format(calendar.getTime()));

        categoryListAdapter = new DashboardCategoryStatsAdapter(this, Collections.<Category>emptyList());
        categoryListNoData = (TextView) findViewById(R.id.category_list_no_data);
        categoryList = (ListView) findViewById(R.id.category_list);
        categoryList.setAdapter(categoryListAdapter);
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);
                Intent i = new Intent(getApplicationContext(), ExpenseListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_CATEGORY_ID, category.getName());
                bundle.putLong(Constants.EXTRAS_FILTER_FROM, monthStart);
                bundle.putLong(Constants.EXTRAS_FILTER_UNTIL, monthEnd);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
        registerForContextMenu(categoryList);

        //INIT DATE BACK & FORWARD BUTTON LISTENERS
        Button backButton = (Button) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                monthStart = Util.getMonthStart(calendar);
                monthEnd = Util.getMonthEnd(calendar);

                new LoadCategoriesTask(MainActivity.this).execute();
                dateRange.setText(formatter.format(calendar.getTime()));
            }
        });
        Button forwardButton = (Button) findViewById(R.id.forward);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                monthStart = Util.getMonthStart(calendar);
                monthEnd = Util.getMonthEnd(calendar);

                new LoadCategoriesTask(MainActivity.this).execute();
                dateRange.setText(formatter.format(calendar.getTime()));
            }
        });
    }

    @Override
    protected ProgressTask asyncDbTask() {
        return new LoadCategoriesTask(this);
    }

    //TASK FOR ASYNC LOAD DATA FROM DB
    private class LoadCategoriesTask extends ProgressTask {
        public LoadCategoriesTask(Activity activity) {
            super(activity);
        }

        @Override
        protected Object process() {
            return DatabaseManager.getInstance(MainActivity.this).getCategoriesWithTransactionSums(monthStart, monthEnd);
        }

        @Override
        protected void callback(Object result) {
            final List<Category> categories = (List<Category>) result;
            if (categories.isEmpty()) {
                categoryList.setVisibility(View.GONE);
                categoryListNoData.setVisibility(View.VISIBLE);
                categoryListNoData.setText(R.string.no_categories);
            } else {
                categoryList.setVisibility(View.VISIBLE);
                categoryListNoData.setVisibility(View.GONE);
            }
            categoryListAdapter.reset(categories);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        } else if (id == R.id.nav_expenses) {
            startActivity(new Intent(getApplicationContext(), ExpenseListActivity.class));
        } else if (id == R.id.nav_revenues) {
            startActivity(new Intent(getApplicationContext(), RevenueListActivity.class));
        } else if (id == R.id.nav_categories) {
            startActivity(new Intent(getApplicationContext(), CategoryListActivity.class));
        } else if (id == R.id.nav_accounts) {
            startActivity(new Intent(getApplicationContext(), AccountListActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if (id == R.id.nav_import) {
            impexTask = new ImportTask(MainActivity.this);
            impexTask.execute();
        } else if (id == R.id.nav_export) {
            impexTask = new ExportTask(MainActivity.this);
            impexTask.execute();
        } else if (id == R.id.nav_about) {
            new AlertDialog.Builder(this)
                    .setView(R.layout.main_about)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (impexTask != null && impexTask.getStatus() == AsyncTask.Status.RUNNING) {
            impexTask.cancel(true);
        }
        super.onDestroy();
    }
}

package protect.cashbox.category;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import protect.cashbox.CashboxActivity;
import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.Mode;
import protect.cashbox.util.ProgressTask;

public class CategoryListActivity extends CashboxActivity {

    private CategoryListAdapter categoryListAdapter;
    private ListView categoryList;
    private TextView categoryListNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.category_list_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //INIT FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.category_fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), CategoryDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.ADD.toString());
                    i.putExtras(bundle);
                    startActivity(i);
                }
            });
        }

        //INIT LAYOUT REFERENCES
        categoryListNoData = (TextView) findViewById(R.id.category_list_no_data);
        categoryList = (ListView) findViewById(R.id.category_list);


        categoryListAdapter = new CategoryListAdapter(this, Collections.<Category>emptyList());
        categoryList.setAdapter(categoryListAdapter);
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) parent.getItemAtPosition(position);

                Intent i = new Intent(getApplicationContext(), CategoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.VIEW.toString());
                bundle.putString(Constants.EXTRAS_ID, category.getName());
                i.putExtras(bundle);
                startActivity(i);
            }
        });
        registerForContextMenu(categoryList);
    }

    @Override
    protected ProgressTask asyncDbTask() {
        return new LoadCategoriesTask(this);
    }

    private class LoadCategoriesTask extends ProgressTask {
        public LoadCategoriesTask(Activity activity) {
            super(activity);
        }
        @Override
        protected Object process() {
            return DatabaseManager.getInstance(CategoryListActivity.this).getCategories();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.category_list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.view_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = (ListView) findViewById(R.id.category_list);
        final Category category = (Category) listView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent i = new Intent(getApplicationContext(), CategoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_ID, category.getName());
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.EDIT.toString());
                i.putExtras(bundle);
                startActivity(i);
                break;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.delete_confirmation_category))
                        .setIcon(R.drawable.ic_delete_white_24dp)
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseManager db = DatabaseManager.getInstance(CategoryListActivity.this);
                                db.deleteCategory(category.getName());
                                new LoadCategoriesTask(CategoryListActivity.this).execute();
                                dialog.dismiss();
                                Toast.makeText(CategoryListActivity.this, getString(R.string.delete_success_category), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            default:
                Log.w(Constants.TAG, getString(R.string.unknown_action) + item.getItemId());
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                categoryListAdapter.filter(searchQuery.trim());
                categoryList.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                categoryListAdapter.reset();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //DO NOTHING
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
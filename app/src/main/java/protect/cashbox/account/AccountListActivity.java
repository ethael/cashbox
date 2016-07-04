package protect.cashbox.account;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

public class AccountListActivity extends CashboxActivity {

    private ListView accountList;
    private TextView accountListNoData;
    private AccountListAdapter accountListAdapter;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.account_list_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.account_list_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //INIT FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.account_fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), AccountDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.ADD.toString());
                    i.putExtras(bundle);
                    startActivity(i);
                }
            });
        }

        //INIT LAYOUT REFERENCES
        accountListNoData = (TextView) findViewById(R.id.account_list_no_data);
        accountList = (ListView) findViewById(R.id.account_list);

        //INIT LIST WITH ADAPTER AND LISTENER
        accountListAdapter = new AccountListAdapter(AccountListActivity.this, Collections.<Account>emptyList());
        accountList.setAdapter(accountListAdapter);
        accountList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Account account = (Account) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), AccountDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_ID, account.getName());
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.VIEW.toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        registerForContextMenu(accountList);
    }

    private class LoadAccountsTask extends ProgressTask {
        public LoadAccountsTask(Activity activity) {
            super(activity);
        }
        @Override
        protected Object process() {
            return DatabaseManager.getInstance(AccountListActivity.this).getAccounts();
        }
        @Override
        protected void callback(Object result) {
            List<Account> accounts = (List<Account>) result;
            if (accounts.isEmpty()) {
                accountList.setVisibility(View.GONE);
                accountListNoData.setVisibility(View.VISIBLE);
                accountListNoData.setText(R.string.no_accounts);
            } else {
                accountList.setVisibility(View.VISIBLE);
                accountListNoData.setVisibility(View.GONE);
            }
            accountListAdapter.reset(accounts);
        }
    }

    @Override
    protected ProgressTask asyncDbTask() {
        return new LoadAccountsTask(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.account_list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.view_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = (ListView) findViewById(R.id.account_list);
        final Account account = (Account) listView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent i = new Intent(getApplicationContext(), AccountDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_ID, account.getName());
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.EDIT.toString());
                i.putExtras(bundle);
                startActivity(i);
                break;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.delete_confirmation_account))
                        .setIcon(R.drawable.ic_delete_white_24dp)
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseManager db = DatabaseManager.getInstance(AccountListActivity.this);
                                db.deleteAccount(account.getName());
                                new LoadAccountsTask(AccountListActivity.this).execute();
                                dialog.dismiss();
                                Toast.makeText(AccountListActivity.this, getString(R.string.delete_success_account), Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.account_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                accountListAdapter.filter(searchQuery.trim());
                accountList.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                accountListAdapter.reset();
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
package protect.cashbox.transaction;


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
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import protect.cashbox.CashboxActivity;
import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.DatabaseManager.TransactionTable;
import protect.cashbox.util.Mode;
import protect.cashbox.util.ProgressTask;
import protect.cashbox.util.PurgeTask;
import protect.cashbox.util.Util;

public class ExpenseListActivity extends CashboxActivity {

    private ListView transactionList;
    private TextView transactionListNoData;
    private TransactionListAdapter transactionListAdapter;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.transaction_list_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.transaction_list_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //INIT FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.transaction_fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), TransactionDetailActivity.class);
                    final Bundle b = new Bundle();
                    b.putInt(Constants.EXTRAS_TRANSACTION_TYPE, TransactionTable.EXPENSE);
                    b.putString(Constants.EXTRAS_VIEW_MODE, Mode.ADD.toString());
                    i.putExtras(b);
                    startActivity(i);
                }
            });
        }

        //INIT LAYOUT REFERENCES
        transactionListNoData = (TextView) findViewById(R.id.transaction_list_no_data);
        transactionList = (ListView) findViewById(R.id.transaction_list);

        //INIT LIST WITH ADAPTER AND LISTENER
        transactionListAdapter = new TransactionListAdapter(ExpenseListActivity.this, Collections.<Transaction>emptyList());
        transactionList.setAdapter(transactionListAdapter);
        transactionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction transaction = (Transaction) parent.getItemAtPosition(position);
                Intent i = new Intent(view.getContext(), TransactionDetailActivity.class);
                final Bundle b = new Bundle();
                b.putInt(Constants.EXTRAS_ID, transaction.getId());
                b.putInt(Constants.EXTRAS_TRANSACTION_TYPE, TransactionTable.EXPENSE);
                b.putString(Constants.EXTRAS_VIEW_MODE, Mode.VIEW.toString());
                i.putExtras(b);
                startActivity(i);
            }
        });
        registerForContextMenu(transactionList);
    }

    @Override
    protected ProgressTask asyncTask() {
        return new LoadExpensesTask(this);
    }

    private class LoadExpensesTask extends ProgressTask {
        private String category;

        public LoadExpensesTask(Activity activity) {
            super(activity);
        }

        @Override
        protected Object process() {
            DatabaseManager db = DatabaseManager.getInstance(ExpenseListActivity.this);
            final Bundle b = getIntent().getExtras();
            if (b == null) {
                return db.getExpenses();
            } else {
                category = b.getString(Constants.EXTRAS_CATEGORY_ID, null);
                long filterFrom = b.getLong(Constants.EXTRAS_FILTER_FROM);
                long filterUntil = b.getLong(Constants.EXTRAS_FILTER_UNTIL);
                return db.getExpensesForCategory(category, filterFrom, filterUntil);
            }
        }

        @Override
        protected void callback(Object result) {
            final List<Transaction> transactions = (List<Transaction>) result;
            if (transactions.isEmpty()) {
                transactionList.setVisibility(View.GONE);
                transactionListNoData.setVisibility(View.VISIBLE);

                String message;
                if (category == null) {
                    message = getString(R.string.no_expenses);
                } else {
                    message = String.format(getString(R.string.no_expenses_for_category), category);
                }
                transactionListNoData.setText(message);
            } else {
                transactionList.setVisibility(View.VISIBLE);
                transactionListNoData.setVisibility(View.GONE);
            }
            transactionListAdapter.reset(transactions, getAccountCurrencySymbols());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.transaction_list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.view_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Transaction transaction = (Transaction) transactionList.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent i = new Intent(this, TransactionDetailActivity.class);
                final Bundle b = new Bundle();
                b.putInt(Constants.EXTRAS_ID, transaction.getId());
                b.putInt(Constants.EXTRAS_TRANSACTION_TYPE, TransactionTable.EXPENSE);
                b.putString(Constants.EXTRAS_VIEW_MODE, Mode.EDIT.toString());
                i.putExtras(b);
                startActivity(i);
                break;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.delete_confirmation_transaction))
                        .setIcon(R.drawable.ic_delete_white_24dp)
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseManager db = DatabaseManager.getInstance(ExpenseListActivity.this);
                                db.deleteTransaction(transaction.getId());
                                new LoadExpensesTask(ExpenseListActivity.this).execute();
                                dialog.dismiss();
                                Toast.makeText(ExpenseListActivity.this, getString(R.string.delete_success_transaction), Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.transaction_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                transactionListAdapter.filter(searchQuery.trim());
                transactionList.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                transactionListAdapter.reset();
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
        int id = item.getItemId();

        if (id == R.id.action_purge_receipts) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View view = getLayoutInflater().inflate(R.layout.cleanup_layout, null, false);
            builder.setTitle(R.string.cleanup_help);
            builder.setView(view);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setPositiveButton(R.string.clean, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatePicker picker = (DatePicker) view.findViewById(R.id.endDate);
                    long purgeUntil = Util.getEndOfDayMs(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                    new PurgeTask(ExpenseListActivity.this, purgeUntil).execute();
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
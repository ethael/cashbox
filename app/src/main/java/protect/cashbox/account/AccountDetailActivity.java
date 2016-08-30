package protect.cashbox.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

import protect.cashbox.CashboxActivity;
import protect.cashbox.CashboxApplication;
import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.Mode;
import protect.cashbox.util.ProgressTask;

public class AccountDetailActivity extends CashboxActivity {

    private Mode MODE;

    private String accountName;
    private EditText accountNameField;
    private Spinner accountTypeField;
    private EditText accountOpeningBalanceField;
    private Spinner accountCurrencyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.account_detail_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //INIT LAYOUT REFERENCES
        accountNameField = (EditText) findViewById(R.id.account_name);
        accountTypeField = (Spinner) findViewById(R.id.account_type);
        accountOpeningBalanceField = (EditText) findViewById(R.id.account_opening_balance);
        accountCurrencyField = (Spinner) findViewById(R.id.account_currency);

        //INITIALIZE SPINNERS
        String[] items = new String[]{getString(R.string.cash), getString(R.string.bank_account), getString(R.string.credit_card)};
        accountTypeField.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));
        items = new String[]{
                getString(R.string.eur),
                getString(R.string.usd),
                getString(R.string.cad),
                getString(R.string.gbp),
                getString(R.string.chf),
                getString(R.string.czk),
                getString(R.string.pln),
                getString(R.string.huf)
        };
        accountCurrencyField.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));

        //INIT MODE DEPENDENT STUFF
        final Bundle extraData = getIntent().getExtras();
        if (extraData != null) {
            accountName = extraData.getString(Constants.EXTRAS_ID);
            switch (Mode.valueOf(extraData.getString(Constants.EXTRAS_VIEW_MODE))) {
                case EDIT:
                    MODE = Mode.EDIT;
                    setTitle(R.string.edit_account);
                    accountNameField.setEnabled(false);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    break;
                case VIEW:
                    MODE = Mode.VIEW;
                    setTitle(R.string.view_account);
                    accountNameField.setEnabled(false);
                    accountTypeField.setEnabled(false);
                    accountOpeningBalanceField.setEnabled(false);
                    accountCurrencyField.setEnabled(false);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //DO NOT SHOW KEYBOARD
                    break;
                case ADD:
                    MODE = Mode.ADD;
                    setTitle(R.string.add_account);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    break;
                default:
                    Log.w(Constants.TAG, getString(R.string.unknown_mode) + extraData.getString(Constants.EXTRAS_VIEW_MODE));
                    break;
            }
        }
    }

    @Override
    protected ProgressTask asyncTask() {
        //LOAD DATA IF NOT IN ADD MODE
        return MODE == Mode.ADD ? null : new ProgressTask(this) {
            @Override
            protected Object process() {
                return DatabaseManager.getInstance(AccountDetailActivity.this).getAccount(accountName);
            }

            @Override
            protected void callback(Object result) {
                Account existingAccount = (Account) result;
                accountNameField.setText(accountName);
                accountOpeningBalanceField.setText(String.format(Locale.getDefault(), "%.2f", existingAccount.getOpeningBalance()));
                accountTypeField.setSelection(existingAccount.getType());
                accountCurrencyField.setSelection(existingAccount.getCurrency());
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (MODE == Mode.VIEW) {
            getMenuInflater().inflate(R.menu.view_menu, menu);
        }
        if (MODE == Mode.EDIT) {
            getMenuInflater().inflate(R.menu.edit_menu, menu);
        }
        if (MODE == Mode.ADD) {
            getMenuInflater().inflate(R.menu.add_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                finish();

                Intent i = new Intent(getApplicationContext(), AccountDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_ID, accountName);
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.EDIT.toString());
                i.putExtras(bundle);
                startActivity(i);
                return true;
            case R.id.action_save:
                String name = accountNameField.getText().toString();
                float balance = Float.parseFloat(accountOpeningBalanceField.getText().toString());

                if (name.isEmpty()) {
                    Toast.makeText(this, R.string.account_name_missing, Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    DatabaseManager db = DatabaseManager.getInstance(AccountDetailActivity.this);
                    Account a = new Account();
                    a.setName(name);
                    a.setOpeningBalance(balance);
                    a.setType(accountTypeField.getSelectedItemPosition());
                    a.setCurrency(accountCurrencyField.getSelectedItemPosition());

                    if (MODE == Mode.ADD) {
                        db.insertAccount(a);
                    }
                    if (MODE == Mode.EDIT) {
                        db.updateAccount(a);
                    }
                    finish();
                    Toast.makeText(AccountDetailActivity.this, getString(R.string.save_success_account), Toast.LENGTH_SHORT).show();
                    ((CashboxApplication) getApplication()).loadAccountCurrencySymbols(true); //RELOAD CURRENCY SYMBOL CACHE
                }
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.delete_confirmation_account))
                        .setIcon(R.drawable.ic_delete_white_24dp)
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseManager db = DatabaseManager.getInstance(AccountDetailActivity.this);
                                db.deleteAccount(accountName);
                                dialog.dismiss();
                                finish();
                                Toast.makeText(AccountDetailActivity.this, getString(R.string.delete_success_account), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

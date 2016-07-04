package protect.cashbox.transaction;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import protect.cashbox.CashboxActivity;
import protect.cashbox.CashboxApplication;
import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.DatabaseManager.TransactionTable;
import protect.cashbox.util.Mode;
import protect.cashbox.util.ProgressTask;

public class TransactionDetailActivity extends CashboxActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSIONS_REQUEST_CAMERA = 2;

    private Mode MODE;
    private String capturedUncommittedReceipt = null;
    private int transactionId;
    private  List<String> categoryNames;
    private  List<String> accountNames;

    private EditText nameField;
    private EditText valueField;
    private EditText noteField;
    private TextView receiptLocationField;
    private TextView currencyField;
    private Spinner categorySpinner;
    private Spinner accountSpinner;
    private EditText dateField;
    private View receiptLayout;
    private View noReceiptButtonLayout;
    private View hasReceiptButtonLayout;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_detail_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //INIT LAYOUT REFERENCES
        nameField = (EditText) findViewById(R.id.name);
        valueField = (EditText) findViewById(R.id.transaction_value);
        currencyField = (TextView) findViewById(R.id.transaction_currency);
        noteField = (EditText) findViewById(R.id.note);
        receiptLocationField = (TextView) findViewById(R.id.receipt_location);
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        accountSpinner = (Spinner) findViewById(R.id.account_spinner);
        dateField = (EditText) findViewById(R.id.date);
        receiptLayout = findViewById(R.id.receipt_layout);
        noReceiptButtonLayout = findViewById(R.id.receipt_null);
        hasReceiptButtonLayout = findViewById(R.id.receipt_not_null);
        updateButton = (Button) findViewById(R.id.update_button);
        Button captureButton = (Button) findViewById(R.id.capture_button);
        Button viewButton = (Button) findViewById(R.id.view_button);

        //LOAD ACTIVITY EXTRA DATA
        Bundle extraData = getIntent().getExtras();
        transactionId = extraData.getInt(Constants.EXTRAS_ID);
        int type = extraData.getInt(Constants.EXTRAS_TRANSACTION_TYPE);

        //HIDE CATEGORY FIELD IF REVENUE
        findViewById(R.id.category_field).setVisibility(type == TransactionTable.REVENUE ? View.GONE : View.VISIBLE);

        //INITIALIZE SPINNERS
        DatabaseManager db = DatabaseManager.getInstance(TransactionDetailActivity.this);
        categoryNames = db.getCategoryNames();
        if (categorySpinner != null && categorySpinner.getCount() == 0) {
            ArrayAdapter<String> categories = new ArrayAdapter<>(this, R.layout.spinner_textview, categoryNames);
            categorySpinner.setAdapter(categories);
        }
        accountNames = db.getAccountNames();
        if (accountSpinner != null && accountSpinner.getCount() == 0) {
            ArrayAdapter<String> accounts = new ArrayAdapter<>(this, R.layout.spinner_textview, accountNames);
            accountSpinner.setAdapter(accounts);
        }

        //INIT DATE FIELD
        final Calendar date = new GregorianCalendar();
        final DateFormat formatter = SimpleDateFormat.getDateInstance();
        dateField.setText(formatter.format(date.getTime()));
        dateField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    new DatePickerDialog(
                            TransactionDetailActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int day) {
                                    date.set(year, month, day);
                                    dateField.setText(formatter.format(date.getTime()));
                                }
                            },
                            date.get(Calendar.YEAR),
                            date.get(Calendar.MONTH),
                            date.get(Calendar.DATE)
                    ).show();
                }
            }
        });

        //INITIALIZE RECEIPT RELATED BUTTONS
        View.OnClickListener captureCallback = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(TransactionDetailActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    captureReceipt();
                } else {
                    ActivityCompat.requestPermissions(TransactionDetailActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSIONS_REQUEST_CAMERA);
                }
            }
        };

        captureButton.setOnClickListener(captureCallback);
        updateButton.setOnClickListener(captureCallback);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receipt = ((TextView) findViewById(R.id.receipt_location)).getText().toString();
                if (capturedUncommittedReceipt != null) {
                    receipt = capturedUncommittedReceipt;
                }

                Intent i = new Intent(v.getContext(), ReceiptDetailActivity.class);
                final Bundle b = new Bundle();
                b.putString("receipt", receipt);
                i.putExtras(b);
                startActivity(i);
            }
        });

        //INIT MODE DEPENDENT STUFF
        switch (Mode.valueOf(extraData.getString(Constants.EXTRAS_VIEW_MODE))) {
            case EDIT:
                MODE = Mode.EDIT;
                setTitle(type == TransactionTable.EXPENSE ? R.string.edit_expense_transaction : R.string.edit_revenue_transaction);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;
            case VIEW:
                MODE = Mode.VIEW;
                setTitle(type == TransactionTable.EXPENSE ? R.string.view_expense_transaction : R.string.view_revenue_transaction);
                categorySpinner.setEnabled(false);
                accountSpinner.setEnabled(false);
                nameField.setEnabled(false);
                valueField.setEnabled(false);
                noteField.setEnabled(false);
                dateField.setEnabled(false);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //DO NOT SHOW KEYBOARD
                break;
            case ADD:
                MODE = Mode.ADD;
                setTitle(type == TransactionTable.EXPENSE ? R.string.add_expense_transaction : R.string.add_revenue_transaction);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                receiptLayout.setVisibility(View.VISIBLE);
                if (capturedUncommittedReceipt == null) {
                    noReceiptButtonLayout.setVisibility(View.VISIBLE);
                    hasReceiptButtonLayout.setVisibility(View.GONE);
                } else {
                    noReceiptButtonLayout.setVisibility(View.GONE);
                    hasReceiptButtonLayout.setVisibility(View.VISIBLE);
                    updateButton.setVisibility(View.VISIBLE);
                }
                break;
            default:
                Log.w(Constants.TAG, getString(R.string.unknown_mode) + extraData.getString(Constants.EXTRAS_VIEW_MODE));
                break;
        }
    }

    @Override
    protected ProgressTask asyncDbTask() {
        //LOAD DATA IF NOT IN ADD MODE
        return MODE == Mode.ADD ? null : new ProgressTask(this) {
            @Override
            protected Object process() {
                return DatabaseManager.getInstance(TransactionDetailActivity.this).getTransaction(transactionId);
            }

            @Override
            protected void callback(Object result) {
                Transaction transaction = (Transaction) result;
                nameField.setText(transaction.getName());
                valueField.setText(String.format(Locale.getDefault(), "%.2f", transaction.getValue()));
                currencyField.setText(Constants.CURRENCY_SYMBOLS.get(getAccountCurrencySymbols().get(transaction.getAccount())));
                noteField.setText(transaction.getNote());
                dateField.setText(SimpleDateFormat.getDateInstance().format(new Date(transaction.getDate())));
                receiptLocationField.setText(transaction.getReceipt());

                int categoryIndex = categoryNames.indexOf(transaction.getCategory());
                if (categoryIndex >= 0) {
                    categorySpinner.setSelection(categoryIndex);
                }

                int accountIndex = accountNames.indexOf(transaction.getAccount());
                if (accountIndex >= 0) {
                    accountSpinner.setSelection(accountIndex);
                }

                if (MODE == Mode.VIEW) {
                    noReceiptButtonLayout.setVisibility(View.GONE);

                    if (transaction.getReceipt() != null && !transaction.getReceipt().isEmpty()) {
                        receiptLayout.setVisibility(View.VISIBLE);
                        hasReceiptButtonLayout.setVisibility(View.VISIBLE);
                    } else {
                        receiptLayout.setVisibility(View.GONE);
                    }
                } else {
                    // If editing a transaction, always list the receipt field
                    receiptLayout.setVisibility(View.VISIBLE);
                    if (transaction.getReceipt() == null || (transaction.getReceipt().isEmpty() && capturedUncommittedReceipt == null)) {
                        noReceiptButtonLayout.setVisibility(View.VISIBLE);
                        hasReceiptButtonLayout.setVisibility(View.GONE);
                    } else {
                        noReceiptButtonLayout.setVisibility(View.GONE);
                        hasReceiptButtonLayout.setVisibility(View.VISIBLE);
                        updateButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
    }

    private void captureReceipt() {
        if (capturedUncommittedReceipt != null) {
            Log.i(Constants.TAG, "Deleting unsaved image: " + capturedUncommittedReceipt);
            if (!new File(capturedUncommittedReceipt).delete()) {
                Log.e(Constants.TAG, "Unable to delete unnecessary file: " + capturedUncommittedReceipt);
            }
            capturedUncommittedReceipt = null;
        }

        String errorMessage = null;
        Intent captureReceiptIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (captureReceiptIntent.resolveActivity(getPackageManager()) == null) {
            errorMessage = "Could not find an activity to take a picture";
        }
        if (imageDir == null) {
            errorMessage = "Failed to locate directory for pictures";
        }
        if (!imageDir.exists() && !imageDir.mkdirs()) {
            errorMessage = "Failed to create receipts image directory";
        }
        if (errorMessage != null) {
            Log.e(Constants.TAG, errorMessage);
            Toast.makeText(this, R.string.receipt_capture_error, Toast.LENGTH_LONG).show();
            return;
        }

        File receiptFile = new File(imageDir, UUID.randomUUID().toString() + ".png");
        captureReceiptIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(receiptFile));
        capturedUncommittedReceipt = receiptFile.getAbsolutePath();
        startActivityForResult(captureReceiptIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onDestroy() {
        if (capturedUncommittedReceipt != null) {
            if (!new File(capturedUncommittedReceipt).delete()) {
                Log.e(Constants.TAG, "Unable to delete unnecessary file: " + capturedUncommittedReceipt);
            }
            capturedUncommittedReceipt = null;
        }
        super.onDestroy();
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
        int id = item.getItemId();

        final Bundle b = getIntent().getExtras();
        final int transactionId = b.getInt(Constants.EXTRAS_ID);
        final int type = b.getInt(Constants.EXTRAS_TRANSACTION_TYPE);

        switch (id) {
            case R.id.action_edit:
                finish();

                Intent i = new Intent(getApplicationContext(), TransactionDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.EXTRAS_ID, transactionId);
                bundle.putInt(Constants.EXTRAS_TRANSACTION_TYPE, type);
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.EDIT.toString());
                i.putExtras(bundle);
                startActivity(i);
                return true;

            case R.id.action_save:
                final String name = nameField.getText().toString();
                if (name.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.transaction_name_missing, Toast.LENGTH_LONG).show();
                    return false;
                }

                final String category = (String) categorySpinner.getSelectedItem();
                if (category == null) {
                    Toast.makeText(getApplicationContext(), R.string.category_missing, Toast.LENGTH_LONG).show();
                    return false;
                }

                final String account = (String) accountSpinner.getSelectedItem();
                if (account == null) {
                    Toast.makeText(getApplicationContext(), R.string.account_missing, Toast.LENGTH_LONG).show();
                    return false;
                }

                final String valueStr = valueField.getText().toString();
                if (valueStr.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.transaction_value_missing, Toast.LENGTH_LONG).show();
                    return false;
                }

                double value;
                try {
                    value = Double.parseDouble(valueStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), R.string.transaction_value_invalid, Toast.LENGTH_LONG).show();
                    return false;
                }

                final String note = noteField.getText().toString();

                EditText dateField = (EditText) findViewById(R.id.date);
                final String dateStr = dateField.getText().toString();
                final DateFormat dateFormatter = SimpleDateFormat.getDateInstance();
                long date;
                try {
                    date = dateFormatter.parse(dateStr).getTime();
                } catch (ParseException e) {
                    Toast.makeText(getApplicationContext(), R.string.transaction_date_invalid, Toast.LENGTH_LONG).show();
                    return false;
                }

                //DELETE OLD RECEIPT IF EXISTS
                String receipt = null;
                if (capturedUncommittedReceipt != null &&
                        receiptLocationField.getText() != null &&
                        !receiptLocationField.getText().toString().trim().isEmpty()) {
                    if (!new File(receiptLocationField.getText().toString()).delete()) {
                        Log.e(Constants.TAG, "Unable to delete old receipt file: " + capturedUncommittedReceipt);
                    }
                    receipt = capturedUncommittedReceipt;
                    capturedUncommittedReceipt = null;
                }

                DatabaseManager db = DatabaseManager.getInstance(TransactionDetailActivity.this);
                Transaction t = new Transaction();
                t.setId(transactionId);
                t.setName(name);
                t.setType(type);
                t.setAccount(account);
                t.setCategory(type == TransactionTable.REVENUE ? null : category);
                t.setValue(value);
                t.setNote(note);
                t.setDate(date);
                t.setReceipt(receipt);

                if (MODE == Mode.EDIT) {
                    db.updateTransaction(t);
                }
                if (MODE == Mode.ADD) {
                    db.insertTransaction(t);
                }
                finish();
                Toast.makeText(TransactionDetailActivity.this, getString(R.string.save_success_transaction), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.delete_confirmation_transaction))
                        .setIcon(R.drawable.ic_delete_white_24dp)
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseManager.getInstance(TransactionDetailActivity.this).deleteTransaction(transactionId);
                                dialog.dismiss();
                                finish();
                                Toast.makeText(TransactionDetailActivity.this, getString(R.string.delete_success_transaction), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Log.i(Constants.TAG, "Image file saved: " + capturedUncommittedReceipt);
            } else {
                capturedUncommittedReceipt = null;
                Log.e(Constants.TAG, "Failed to create receipt image: " + resultCode);
            }
            ((CashboxApplication) this.getApplication()).stopActivityTransitionTimer();
            onResume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureReceipt();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_camera_permission_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}

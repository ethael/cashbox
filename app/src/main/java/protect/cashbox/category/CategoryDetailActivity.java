package protect.cashbox.category;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import protect.cashbox.CashboxActivity;
import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.Mode;
import protect.cashbox.util.ProgressTask;

public class CategoryDetailActivity extends CashboxActivity {

    private Mode MODE;

    private String categoryName;
    private EditText categoryNameField;
    private EditText categoryMaxValueField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_detail_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //INIT LAYOUT REFERENCES
        categoryNameField = (EditText) findViewById(R.id.category_name);
        categoryMaxValueField = (EditText) findViewById(R.id.transactionSummary);

        //INIT MODE DEPENDENT STUFF
        Bundle extraData = getIntent().getExtras();
        if (extraData != null) {
            categoryName = extraData.getString(Constants.EXTRAS_ID);
            switch (Mode.valueOf(extraData.getString(Constants.EXTRAS_VIEW_MODE))) {
                case EDIT:
                    MODE = Mode.EDIT;
                    setTitle(R.string.edit_category);
                    categoryNameField.setEnabled(false);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    break;
                case VIEW:
                    MODE = Mode.VIEW;
                    setTitle(R.string.view_category);
                    categoryNameField.setEnabled(false);
                    categoryMaxValueField.setEnabled(false);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //DO NOT SHOW KEYBOARD
                    break;
                case ADD:
                    MODE = Mode.ADD;
                    setTitle(R.string.add_category);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    break;
                default:
                    Log.w(Constants.TAG, getString(R.string.unknown_mode) + extraData.getString(Constants.EXTRAS_VIEW_MODE));
                    break;
            }
        }
    }

    @Override
    protected ProgressTask asyncDbTask() {
        //LOAD DATA IF NOT IN ADD MODE
        return MODE == Mode.ADD ? null : new ProgressTask(this) {
            @Override
            protected Object process() {
                return DatabaseManager.getInstance(CategoryDetailActivity.this).getCategory(categoryName);
            }

            @Override
            protected void callback(Object result) {
                Category existingCategory = (Category) result;
                categoryNameField.setText(categoryName);
                categoryMaxValueField.setText(String.format(Locale.getDefault(), "%d", existingCategory.getMax()));
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

                Intent i = new Intent(getApplicationContext(), CategoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRAS_ID, categoryName);
                bundle.putString(Constants.EXTRAS_VIEW_MODE, Mode.EDIT.toString());
                i.putExtras(bundle);
                startActivity(i);
                return true;

            case R.id.action_save:
                String name = categoryNameField.getText().toString();

                int max;
                try {
                    max = Integer.parseInt(categoryMaxValueField.getText().toString());
                } catch (NumberFormatException e) {
                    max = 0;
                }

                if (name.length() > 0) {
                    DatabaseManager db = DatabaseManager.getInstance(CategoryDetailActivity.this);
                    Category c = new Category();
                    c.setName(name);
                    c.setMax(max);

                    if (MODE == Mode.ADD) {
                        db.insertCategory(c);
                    }
                    if (MODE == Mode.EDIT) {
                        db.updateCategory(c);
                    }
                    finish();
                    Toast.makeText(CategoryDetailActivity.this, getString(R.string.save_success_category), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.category_name_missing, Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.delete_confirmation_category))
                        .setIcon(R.drawable.ic_delete_white_24dp)
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseManager db = DatabaseManager.getInstance(CategoryDetailActivity.this);
                                db.deleteCategory(categoryName);
                                dialog.dismiss();
                                finish();
                                Toast.makeText(CategoryDetailActivity.this, getString(R.string.delete_success_category), Toast.LENGTH_SHORT).show();
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

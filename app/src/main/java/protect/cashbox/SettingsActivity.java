package protect.cashbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Toast;

import protect.cashbox.util.Constants;
import protect.cashbox.util.ProgressTask;

public class SettingsActivity extends CashboxActivity {

    private CheckBox themeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.account_detail_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //INIT LAYOUT REFERENCES
        themeField = (CheckBox) findViewById(R.id.settings_theme);
    }

    @Override
    public void onResume() {
        super.onResume();

        //PRESET VIEW BASED ON SHARED PREFERENCES
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.SETTINGS_LIGHT_THEME, false)) {
            themeField.setChecked(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean(Constants.SETTINGS_LIGHT_THEME, themeField.isChecked());
                editor.apply();

                Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_LONG).show();

                //RESTART ACTIVITY TO NEW THEME
                finish();
                final Intent intent = getIntent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected ProgressTask asyncDbTask() {
        //NO NEED FOR ASYNC LOAD
        return null;
    }
}

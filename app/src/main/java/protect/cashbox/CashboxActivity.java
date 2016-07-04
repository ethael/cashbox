package protect.cashbox;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.ProgressTask;

public abstract class CashboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //SET CORRECT THEME BASED ON SAVED PREFERENCES
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.SETTINGS_LIGHT_THEME, false)) {
            setTheme(R.style.AppTheme_Light);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        //SHOW PASSWORD DIALOG IF PASSWORD IS SET AND APP WAS IN BACKGROUND
        CashboxApplication app = (CashboxApplication) this.getApplication();
        if (app.wasInBackground) {
            DatabaseManager.getInstance(this).lock();
            showPasswordDialog();
        } else {
            ProgressTask task = asyncDbTask();
            if (task != null) {
                task.execute();
            }
        }
        app.stopActivityTransitionTimer();
    }


    @Override
    public void onPause() {
        super.onPause();
        ((CashboxApplication) this.getApplication()).startActivityTransitionTimer();
    }

    protected abstract ProgressTask asyncDbTask();

    protected void showPasswordDialog() {
        //SET CORRECT THEME BASED ON SAVED PREFERENCES
        int lightTheme = android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen;
        int darkTheme = android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen;
        boolean lightThemePreference = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.SETTINGS_LIGHT_THEME, false);
        final Dialog dialog = new Dialog(this, lightThemePreference ? lightTheme : darkTheme);
        dialog.setContentView(R.layout.password_dialog);
        dialog.setTitle(R.string.password);
        dialog.setCancelable(false);
        dialog.show();

        final EditText passwordField = (EditText) dialog.findViewById(R.id.password_field);
        final Button positive = (Button) dialog.findViewById(R.id.button_ok);
        final Button negative = (Button) dialog.findViewById(R.id.button_cancel);

        positive.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                DatabaseManager db = DatabaseManager.getInstance(getBaseContext());
                if (db.unlock(passwordField.getText().toString())) {
                    dialog.dismiss();
                    //LOAD DATA ASYNCHRONOUSLY IF NEEDED
                    ProgressTask task = asyncDbTask();
                    if (task != null) {
                        task.execute();
                    }
                } else {
                    passwordField.startAnimation(AnimationUtils.loadAnimation(CashboxActivity.this, R.anim.shake));
                    passwordField.setText("");
                    Toast.makeText(getBaseContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                }
            }
        });

        negative.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    protected Map<String, Integer> getAccountCurrencySymbols() {
        return ((CashboxApplication) getApplication()).loadAccountCurrencySymbols(false);
    }
}

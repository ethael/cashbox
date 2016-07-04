package protect.cashbox;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import protect.cashbox.account.Account;
import protect.cashbox.util.DatabaseManager;

public class CashboxApplication extends Application {

    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    private Map<String, Integer> accountNameToCurrencySymbolMap;
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean wasInBackground = true;

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                CashboxApplication.this.wasInBackground = true;
            }
        };
        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }
        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }
        this.wasInBackground = false;
    }

    public Map<String, Integer> loadAccountCurrencySymbols(boolean cleanCache) {
        if (accountNameToCurrencySymbolMap == null || cleanCache) {
            //CACHE CURRENCY SYMBOLS FOR ACCOUNTS (THEN ACCESSIBLE FROM ALL ACTIVITIES WITHOUT DB REQUEST)
            accountNameToCurrencySymbolMap = new HashMap<>();
            for(Account a : DatabaseManager.getInstance(this).getAccounts()) {
                accountNameToCurrencySymbolMap.put(a.getName(), a.getCurrency());
            }
        }
        return accountNameToCurrencySymbolMap;
    }
}
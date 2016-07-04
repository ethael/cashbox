package protect.cashbox.util;

import android.util.Log;

import java.util.Calendar;

import protect.cashbox.R;

public class Util {

    public static int currencyIdToLocaleId(int id) {
        switch(id) {
            case Constants.ACCOUNT_CURRENCY_EUR:
                return R.string.eur;
            case Constants.ACCOUNT_CURRENCY_USD:
                return R.string.usd;
            case Constants.ACCOUNT_CURRENCY_CAD:
                return R.string.cad;
            case Constants.ACCOUNT_CURRENCY_GBP:
                return R.string.gbp;
            case Constants.ACCOUNT_CURRENCY_CHF:
                return R.string.chf;
            case Constants.ACCOUNT_CURRENCY_CZK:
                return R.string.czk;
            case Constants.ACCOUNT_CURRENCY_PLN:
                return R.string.pln;
            case Constants.ACCOUNT_CURRENCY_HUF:
                return R.string.huf;
            default:
                Log.e(Constants.TAG, "Failed to find localised message for currency ID. Unknown currency ID: " + id);
                return R.string.currency_unknown;
        }
    }

    public static int accountTypeToLocaleId(int type) {
        switch(type) {
            case Constants.ACCOUNT_TYPE_CASH:
                return R.string.cash;
            case Constants.ACCOUNT_TYPE_BANK_ACCOUNT:
                return R.string.bank_account;
            case Constants.ACCOUNT_TYPE_CREDIT_CARD:
                return R.string.credit_card;
            default:
                Log.e(Constants.TAG, "Failed to find localised message for account type. Unknown account type: " + type);
                return R.string.account_type_unknown;
        }
    }

    public static long getMonthStart(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
        return c.getTimeInMillis();
    }

    public static long getMonthEnd(Calendar c) {
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, c.getActualMaximum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
        return c.getTimeInMillis();
    }

    public static long getStartOfDayMs(int year, int month, int day) {
        final Calendar date = Calendar.getInstance();

        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR_OF_DAY, date.getActualMinimum(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, date.getActualMinimum(Calendar.MINUTE));
        date.set(Calendar.SECOND, date.getActualMinimum(Calendar.SECOND));
        date.set(Calendar.MILLISECOND, date.getActualMinimum(Calendar.MILLISECOND));
        return date.getTimeInMillis();
    }

    public static long getEndOfDayMs(int year, int month, int day) {
        final Calendar date = Calendar.getInstance();

        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR_OF_DAY, date.getActualMaximum(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, date.getActualMaximum(Calendar.MINUTE));
        date.set(Calendar.SECOND, date.getActualMaximum(Calendar.SECOND));
        date.set(Calendar.MILLISECOND, date.getActualMaximum(Calendar.MILLISECOND));
        return date.getTimeInMillis();
    }
}

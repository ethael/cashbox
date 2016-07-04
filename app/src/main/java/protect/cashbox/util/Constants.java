package protect.cashbox.util;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final String TAG = "Cashbox";
    public static final String ACCOUNT = "ACCOUNT";
    public static final String CATEGORY = "CATEGORY";
    public static final String TRANSACTION = "TRANSACTION";
    public static final String SETTING = "SETTING";

    public static final int ACCOUNT_TYPE_CASH = 0;
    public static final int ACCOUNT_TYPE_BANK_ACCOUNT = 1;
    public static final int ACCOUNT_TYPE_CREDIT_CARD = 2;

    public static final int ACCOUNT_CURRENCY_EUR = 0;
    public static final int ACCOUNT_CURRENCY_USD = 1;
    public static final int ACCOUNT_CURRENCY_CAD = 2;
    public static final int ACCOUNT_CURRENCY_GBP = 3;
    public static final int ACCOUNT_CURRENCY_CHF = 4;
    public static final int ACCOUNT_CURRENCY_CZK = 5;
    public static final int ACCOUNT_CURRENCY_PLN = 6;
    public static final int ACCOUNT_CURRENCY_HUF = 7;

    public static final String EXTRAS_VIEW_MODE = "mode";
    public static final String EXTRAS_FILTER_FROM = "from";
    public static final String EXTRAS_FILTER_UNTIL = "to";
    public static final String EXTRAS_ID = "id";
    public static final String EXTRAS_CATEGORY_ID = "category";
    public static final String EXTRAS_TRANSACTION_TYPE = "type";
    public static final java.lang.String EXTRAS_RECEIPT_FILE_NAME = "receipt";

    public static final String SETTINGS_LIGHT_THEME = "settings_light_theme";

    public static final List<String> CURRENCY_SYMBOLS = new ArrayList<String>() {
        {
            add("€");
            add("$");
            add("$");
            add("£");
            add("Fr");
            add("Kc");
            add("zł");
            add("Ft");
        }
    };
}

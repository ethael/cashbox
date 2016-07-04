package protect.cashbox;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import protect.cashbox.category.CategoryDetailActivity;
import protect.cashbox.util.DatabaseManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class TransactionListFragmentAdapterTest {
    private Activity activity;
    private DatabaseManager db;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(CategoryDetailActivity.class);
        db = DatabaseManager.getInstance(activity);
    }

    @Test
    public void checkTransaction() {
//        final String NAME = "name";
//        final String ACCOUNT = "account";
//        final String BUDGET = "category";
//        final double VALUE = 100.50;
//        final String NOTE = "note";
//        final long DATE = 0;
//        final String RECEIPT = "receipt";

//        for (boolean hasReceipt : new boolean[]{false, true}) {
//            db.insertTransaction(NAME, DatabaseManager.TransactionTable.EXPENSE, ACCOUNT, BUDGET,
//                    VALUE, NOTE, DATE, hasReceipt ? RECEIPT : "");
//            Cursor cursor = db.getExpenses();
//            cursor.moveToFirst();
//            int transactionId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.TransactionTable.COL_NAME));
//
//            TransactionListAdapter adapter = new TransactionListAdapter(activity, cursor);
//            View view = adapter.newView(activity, cursor, null);
//            adapter.bindView(view, activity, cursor);
//            cursor.close();
//
//            db.deleteTransaction(transactionId);
//
//            TextView nameField = (TextView) view.findViewById(R.id.name);
//            TextView valueField = (TextView) view.findViewById(R.id.account_type);
//            TextView dateField = (TextView) view.findViewById(R.id.date);
//            TextView budgetField = (TextView) view.findViewById(R.id.category);
//            ImageView receiptIcon = (ImageView) view.findViewById(R.id.receiptIcon);
//
//            assertEquals(hasReceipt ? View.VISIBLE : View.GONE, receiptIcon.getVisibility());
//            assertEquals(DESCRIPTION, nameField.getText().toString());
//            assertEquals(BUDGET, budgetField.getText().toString());
//
//            String expectedValue = String.format("%.2f", VALUE);
//            assertEquals(expectedValue, valueField.getText().toString());
//
//            // As the date field may be converted using the current locale,
//            // simply check that there is something there.
//            assertTrue(dateField.getText().length() > 0);
//        }
    }
}

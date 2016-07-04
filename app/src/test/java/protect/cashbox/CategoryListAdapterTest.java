package protect.cashbox;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.util.Calendar;
import java.util.List;

import protect.cashbox.category.Category;
import protect.cashbox.category.CategoryListActivity;
import protect.cashbox.category.CategoryListAdapter;
import protect.cashbox.util.DatabaseManager;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class CategoryListAdapterTest {
    private long nowMs;
    private long lastYearMs;
    private int MONTHS_PER_YEAR = 12;

    @Before
    public void setUp() {
        nowMs = System.currentTimeMillis();

        Calendar lastYear = Calendar.getInstance();
        lastYear.set(Calendar.YEAR, lastYear.get(Calendar.YEAR) - 1);
        lastYearMs = lastYear.getTimeInMillis();
    }

    @Test
    public void TestAdapter() {
        ActivityController activityController = Robolectric.buildActivity(CategoryListActivity.class).create();
        Activity activity = (Activity) activityController.get();

        DatabaseManager db = DatabaseManager.getInstance(activity);
        final String NAME = "name";
        final int BUDGET = 100;
        final int CURRENT = 50;

        //db.insertCategory(NAME, BUDGET);
        //db.insertTransaction("", DatabaseManager.TransactionTable.EXPENSE, "", NAME, CURRENT, "", nowMs, "");

        final int SCALED_BUDGET = BUDGET * (MONTHS_PER_YEAR + 1);

        final List<Category> categories = db.getCategoriesWithTransactionSums(lastYearMs, nowMs);
        final CategoryListAdapter adapter = new CategoryListAdapter(activity, categories);

        View view = adapter.getView(0, null, null);

        final TextView budgetName = (TextView) view.findViewById(R.id.account_name);
        final ProgressBar budgetBar = (ProgressBar) view.findViewById(R.id.category_bar);
        final TextView budgetValue = (TextView) view.findViewById(R.id.category_max);

        assertEquals(NAME, budgetName.getText().toString());
        assertEquals(CURRENT, budgetBar.getProgress());
        assertEquals(SCALED_BUDGET, budgetBar.getMax());

        String fractionFormat = activity.getResources().getString(R.string.fraction);
        String fraction = String.format(fractionFormat, CURRENT, SCALED_BUDGET);
        assertEquals(fraction, budgetValue.getText().toString());
    }
}

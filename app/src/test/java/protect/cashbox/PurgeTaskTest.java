package protect.cashbox;

import android.app.Activity;
import android.os.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import protect.cashbox.transaction.Transaction;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.PurgeTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class PurgeTaskTest {
    private final int NUM_TRANSACTIONS = 10;
    private final String WITH_RECEIPT_NAME = "receipt_exists";
    private final String WITHOUT_RECEIPT_NAME = "receipt_missing";
    File imageDir;
    File missingReceipt;
    File orphanReceipt;
    private Activity activity;
    private DatabaseManager db;

    @Before
    public void setUp() throws IOException {
        activity = Robolectric.setupActivity(MainActivity.class);
        db = DatabaseManager.getInstance(activity);

        imageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        assertNotNull(imageDir);

        boolean result;
        if (imageDir.exists() == false) {
            result = imageDir.mkdirs();
            assertTrue(result);
        }

        missingReceipt = new File(imageDir, "missing");

        orphanReceipt = new File(imageDir, "orphan");
        result = orphanReceipt.createNewFile();
        assertTrue(result);
    }

    /**
     * Add 10 transactions each of the following:
     * - expense
     * * receipt exists
     * * receipt missing
     * - revenue
     * * receipt exists
     * * receipt missing
     */
    private void addTransactions() throws IOException {
//        for (int type : new Integer[]{DatabaseManager.TransactionTable.REVENUE, DatabaseManager.TransactionTable.EXPENSE}) {
//            for (int index = 1; index <= NUM_TRANSACTIONS; index++) {
//                File existingReceipt = new File(imageDir, "exists-" + type + "-" + index);
//                boolean result = existingReceipt.createNewFile();
//                assertTrue(result);
//
//                result = db.insertTransaction(WITH_RECEIPT_NAME,
//                        type,
//                        DatabaseManager.TransactionTable.COL_ACCOUNT,
//                        DatabaseManager.TransactionTable.COL_CATEGORY,
//                        0,
//                        DatabaseManager.TransactionTable.COL_NOTE,
//                        index,
//                        existingReceipt.getAbsolutePath());
//                assertTrue(result);
//
//                result = db.insertTransaction(WITHOUT_RECEIPT_NAME,
//                        type,
//                        DatabaseManager.TransactionTable.COL_ACCOUNT,
//                        DatabaseManager.TransactionTable.COL_CATEGORY,
//                        0,
//                        DatabaseManager.TransactionTable.COL_NOTE,
//                        index,
//                        missingReceipt.getAbsolutePath());
//                assertTrue(result);
//            }
//        }
    }

    @Test
    public void testCleanupOnly() throws IOException {
        addTransactions();

        PurgeTask task = new PurgeTask(activity);
        task.execute();

        // Actually run the task to completion
        Robolectric.flushBackgroundThreadScheduler();

        // Check that the orphaned image is now deleted
        assertEquals(false, orphanReceipt.exists());

        // Check that the database only reports transactions with
        // existing receipts
        List<Transaction> transactions = db.getTransactions(null);

        // There should be NUM_TRANSACTIONS transactions for each of
        // REVENUE and EXPENSE
        assertEquals(NUM_TRANSACTIONS * 2, transactions.size());

        for (Transaction t : transactions) {
            assertEquals(WITH_RECEIPT_NAME, t.getName());

            // Check that the image still exists
            File receipt = new File(t.getReceipt());
            assertEquals(true, receipt.exists());
            assertEquals(true, receipt.isFile());
        }
    }

    @Test
    public void testCleanupAndPurge() throws IOException {
        addTransactions();

        final Long DATE_CUTOFF = (long) NUM_TRANSACTIONS / 2;

        PurgeTask task = new PurgeTask(activity, DATE_CUTOFF);
        task.execute();

        // Actually run the task to completion
        Robolectric.flushBackgroundThreadScheduler();

        // Check that the orphaned image is not deleted
        assertEquals(false, orphanReceipt.exists());

        // Check that the database only reports transactions with
        // existing receipts
        List<Transaction> transactions = db.getTransactions(null);

        // Before the purge there were NUM_TRANSACTIONS transactions for each of
        // REVENUE and EXPENSE. The purge will reduce the count by half.
        assertEquals(NUM_TRANSACTIONS, transactions.size());

        for (Transaction t : transactions) {
            assertEquals(WITH_RECEIPT_NAME, t.getName());

            // Check that the image still exists
            File receipt = new File(t.getReceipt());
            assertEquals(true, receipt.exists());
            assertEquals(true, receipt.isFile());

            assertTrue(t.getDate() > DATE_CUTOFF);
        }
    }
}

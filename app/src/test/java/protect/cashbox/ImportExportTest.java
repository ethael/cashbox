package protect.cashbox;

import android.app.Activity;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import protect.cashbox.category.Category;
import protect.cashbox.category.CategoryDetailActivity;
import protect.cashbox.impex.CsvDatabaseExporter;
import protect.cashbox.impex.CsvDatabaseImporter;
import protect.cashbox.transaction.Transaction;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.impex.ImportTask;
import protect.cashbox.util.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class ImportExportTest {
    private Activity activity;
    private DatabaseManager db;
    private long nowMs;
    private long lastYearMs;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(CategoryDetailActivity.class);
        db = DatabaseManager.getInstance(activity);
        nowMs = System.currentTimeMillis();

        Calendar lastYear = Calendar.getInstance();
        lastYear.set(Calendar.YEAR, lastYear.get(Calendar.YEAR) - 1);
        lastYearMs = lastYear.getTimeInMillis();
    }

    private void addCategories(int categoriesToAdd) {
        // Add in reverse order to test sorting
//        for (int index = categoriesToAdd; index > 0; index--) {
//            String name = String.format(Locale.getDefault(), "category, \"%4d", index);
//            boolean result = db.insertCategory(name, index);
//            assertTrue(result);
//        }

        assertEquals(categoriesToAdd, db.getCategoryCount());
    }

    /**
     * Check that all of the budgets follow the pattern
     * specified in addCategories(), and are in sequential order
     * where the smallest category value is 1
     */
    private void checkBudgets() {
        List<Category> categories = db.getCategoriesWithTransactionSums(lastYearMs, nowMs);
        int index = 1;
        for (Category category : categories) {
            assertEquals(category.getCurrent(), 0);
            assertEquals(category.getMax(), index * (12 + 1)); //12 months in year
            index++;
        }

        List<String> names = db.getCategoryNames();
        index = 1;
        for (String name : names) {
            String expectedName = String.format(Locale.getDefault(), "category, \"%4d", index);
            assertEquals(expectedName, name);
            index++;
        }
    }

    /**
     * Delete the contents of the budgets and transactions databases
     */
    private void clearDatabase() {
        SQLiteDatabase database = db.getWritableDatabase();
        database.execSQL("delete from " + DatabaseManager.CategoryTable.TABLE_NAME);
        database.execSQL("delete from " + DatabaseManager.TransactionTable.TABLE_NAME);
        database.close();

        assertEquals(0, db.getCategoryCount());
        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
    }

    /**
     * Add the given number of revenue and expense transactions.
     * All string fields will be in the format
     * name id
     * where "name" is the name of the field, and "id"
     * is the index for the entry. All numberical fields will
     * be assigned to the index.
     *
     * @param transactionsToAdd Number of transaction to add.
     */
    private void addTransactions(int transactionsToAdd) {
        // Add in increasing order to test sorting later
        for (int type : new Integer[]{DatabaseManager.TransactionTable.REVENUE, DatabaseManager.TransactionTable.EXPENSE}) {
//            for (int index = 1; index <= transactionsToAdd; index++) {
//                boolean result = db.insertTransaction(
//                        String.format(DatabaseManager.TransactionTable.COL_NAME + ", \"%4d", index),
//                        type,
//                        String.format(DatabaseManager.TransactionTable.COL_ACCOUNT + "%4d", index),
//                        String.format(DatabaseManager.TransactionTable.COL_CATEGORY + "%4d", index),
//                        index,
//                        String.format(DatabaseManager.TransactionTable.COL_NOTE + "%4d", index),
//                        index,
//                        String.format(DatabaseManager.TransactionTable.COL_RECEIPT + "%4d", index));
//                assertTrue(result);
//            }
        }
    }

    /**
     * Check that all of the transactions follow the pattern
     * specified in addTransactions(), and are in sequential order
     * from the most recent to the oldest
     */
    private void checkTransactions(boolean wasImported) {
        int index = 0;
        for (Transaction t : db.getExpenses()) {
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_NAME + ", \"%4d", index), t.getName());
            assertEquals(DatabaseManager.TransactionTable.EXPENSE, t.getType());
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_ACCOUNT + "%4d", index), t.getAccount());
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_CATEGORY + "%4d", index), t.getCategory());
            assertEquals(index, (int) t.getValue());
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_NOTE + "%4d", index), t.getNote());
            assertEquals(index, t.getDate());

            if (wasImported) {
                // Receipts cannot be imported, and will always be empty
                assertEquals("", t.getReceipt());
            } else {
                assertEquals(String.format(DatabaseManager.TransactionTable.COL_RECEIPT + "%4d", index), t.getReceipt());
            }
            index++;
        }

        index = 0;
        for (Transaction t : db.getRevenues()) {
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_NAME + ", \"%4d", index), t.getName());
            assertEquals(DatabaseManager.TransactionTable.REVENUE, t.getType());
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_ACCOUNT + "%4d", index), t.getAccount());
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_CATEGORY + "%4d", index), t.getCategory());
            assertEquals(index, (int) t.getValue());
            assertEquals(String.format(DatabaseManager.TransactionTable.COL_NOTE + "%4d", index), t.getNote());
            assertEquals(index, t.getDate());

            if (wasImported) {
                // Receipts cannot be imported, and will always be empty
                assertEquals("", t.getReceipt());
            } else {
                assertEquals(String.format(DatabaseManager.TransactionTable.COL_RECEIPT + "%4d", index), t.getReceipt());
            }
            index++;
        }
    }

    @Test
    public void multipleBudgetsExportImport() throws IOException {
        final int NUM_BUDGETS = 1000;
        addCategories(NUM_BUDGETS);

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outData);
        boolean result = exportData(writer);
        assertTrue(result);
        clearDatabase();

        ByteArrayInputStream inData = new ByteArrayInputStream(outData.toByteArray());
        InputStreamReader reader = new InputStreamReader(inData);
        result = importData(reader);
        assertTrue(result);
        assertEquals(NUM_BUDGETS, db.getCategoryCount());
        checkBudgets();
        clearDatabase();
    }

    @Test
    public void importExistingBudgetsNotReplace() throws IOException {
        final int NUM_BUDGETS = 1000;
        addCategories(NUM_BUDGETS);

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outData);
        boolean result = exportData(writer);
        assertTrue(result);

        ByteArrayInputStream inData = new ByteArrayInputStream(outData.toByteArray());
        InputStreamReader reader = new InputStreamReader(inData);
        result = importData(reader);
        assertTrue(result);
        assertEquals(NUM_BUDGETS, db.getCategoryCount());
        checkBudgets();
        clearDatabase();
    }

    @Test
    public void multipleTransactionsExportImport() throws IOException {
        final int NUM_TRANSACTIONS = 1000;
        addTransactions(NUM_TRANSACTIONS);

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outData);
        boolean result = exportData(writer);
        assertTrue(result);
        clearDatabase();

        ByteArrayInputStream inData = new ByteArrayInputStream(outData.toByteArray());
        InputStreamReader reader = new InputStreamReader(inData);
        result = importData(reader);
        assertTrue(result);
        assertEquals(NUM_TRANSACTIONS, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(NUM_TRANSACTIONS, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
        checkTransactions(true);
        clearDatabase();
    }

    @Test
    public void importExistingTransactionsNotReplace() throws IOException {
        final int NUM_TRANSACTIONS = 1000;
        addTransactions(NUM_TRANSACTIONS);

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outData);
        boolean result = exportData(writer);
        assertTrue(result);

        ByteArrayInputStream inData = new ByteArrayInputStream(outData.toByteArray());
        InputStreamReader reader = new InputStreamReader(inData);
        result = importData(reader);
        assertTrue(result);
        assertEquals(NUM_TRANSACTIONS, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(NUM_TRANSACTIONS, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
        checkTransactions(false);
        clearDatabase();
    }

    @Test
    public void multipleEverythingExportImport() throws IOException {
        final int NUM_ITEMS = 1000;
        addCategories(NUM_ITEMS);
        addTransactions(NUM_ITEMS);

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outData);
        boolean result = exportData(writer);
        assertTrue(result);
        clearDatabase();

        ByteArrayInputStream inData = new ByteArrayInputStream(outData.toByteArray());
        InputStreamReader reader = new InputStreamReader(inData);
        result = importData(reader);
        assertTrue(result);
        assertEquals(NUM_ITEMS, db.getCategoryCount());
        assertEquals(NUM_ITEMS, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(NUM_ITEMS, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
        checkBudgets();
        checkTransactions(true);
        clearDatabase();
    }

    @Test
    public void corruptedImportNothingSaved() throws IOException {
        final int NUM_ITEMS = 1000;
        addCategories(NUM_ITEMS);
        addTransactions(NUM_ITEMS);

        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outData);
        boolean result = exportData(writer);
        assertTrue(result);
        clearDatabase();

        String corruptEntry = "ThisStringIsLikelyNotPartOfAnyFormat";
        ByteArrayInputStream inData = new ByteArrayInputStream((outData.toString() + corruptEntry).getBytes());
        InputStreamReader reader = new InputStreamReader(inData);
        result = importData(reader);
        assertEquals(false, result);
        assertEquals(0, db.getCategoryCount());
        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
    }

    @Test
    public void useImportExportTask() {
        final int NUM_ITEMS = 10;
        addCategories(NUM_ITEMS);
        addTransactions(NUM_ITEMS);

        // Export to whatever the default location is
        ImportTask task = new ImportTask(activity);
        task.execute();

        // Actually run the task to completion
        Robolectric.flushBackgroundThreadScheduler();
        clearDatabase();

        // Import everything back from the default location
        task = new ImportTask(activity);
        task.execute();

        // Actually run the task to completion
        Robolectric.flushBackgroundThreadScheduler();
        assertEquals(NUM_ITEMS, db.getCategoryCount());
        assertEquals(NUM_ITEMS, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(NUM_ITEMS, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
        checkBudgets();
        checkTransactions(true);
        clearDatabase();
    }

    private boolean importData(InputStreamReader reader) {
        try {
            new CsvDatabaseImporter().importData(db, reader);
            return true;
        } catch (IOException | ParseException | InterruptedException e) {
            Log.e(Constants.TAG, "Failed to input data", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                //DO NOTHING
            }
        }
        return false;
    }

    private boolean exportData(OutputStreamWriter writer) {
        try {
            new CsvDatabaseExporter().exportData(db, writer);
            return true;
        } catch (IOException | InterruptedException e) {
            Log.e(Constants.TAG, "Failed to output data", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //DO NOTHING
            }
        }
        return false;
    }
}

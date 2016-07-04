package protect.cashbox;

import android.app.Activity;
import android.content.ContentValues;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import protect.cashbox.category.Category;
import protect.cashbox.category.CategoryDetailActivity;
import protect.cashbox.util.DatabaseManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class DatabaseTest {
    private DatabaseManager db;
    private long nowMs;
    private long lastYearMs;
    private int MONTHS_PER_YEAR = 12;

    @Before
    public void setUp() {
        Activity activity = Robolectric.setupActivity(CategoryDetailActivity.class);
        db = DatabaseManager.getInstance(activity);
        nowMs = System.currentTimeMillis();

        Calendar lastYear = Calendar.getInstance();
        lastYear.set(Calendar.YEAR, lastYear.get(Calendar.YEAR) - 1);
        lastYearMs = lastYear.getTimeInMillis();
    }

//    @Test
//    public void addRemoveOneBudget() {
//        assertEquals(db.getCategoryCount(), 0);
//        boolean result = db.insertCategory("category", 100);
//        assertTrue(result);
//        assertEquals(db.getCategoryCount(), 1);
//
//        Category category = db.getCategory("category");
//        assertNotNull(category);
//        assertEquals("category", category.getName());
//        assertEquals(100, category.getMax());
//        assertEquals(0, category.getCurrent());
//
//        List<Category> categories = db.getCategoriesWithTransactionSums(lastYearMs, nowMs);
//        assertEquals(1, categories.size());
//        assertEquals("category", categories.get(0).getName());
//        assertEquals(100 * (MONTHS_PER_YEAR + 1), categories.get(0).getMax());
//        assertEquals(0, categories.get(0).getCurrent());
//
//        result = db.deleteCategory("category");
//        assertTrue(result);
//        assertEquals(db.getCategoryNames().size(), 0);
//        assertNull(db.getCategory("category"));
//    }
//
//    @Test
//    public void checkTransactionsForBudget() {
//        boolean result = db.insertCategory("category", 100);
//        assertTrue(result);
//
//        final int NUM_EXPENSES = 1000;
//        int expectedCurrent = 0;
//
//        for (int index = 0; index < NUM_EXPENSES; index++) {
//            result = db.insertTransaction("one", DatabaseManager.TransactionTable.EXPENSE, "", "category", index, "", nowMs, "");
//            assertTrue(result);
//            expectedCurrent += index;
//        }
//
//        List<Transaction> expenses = db.getExpenses();
//        assertEquals(NUM_EXPENSES, expenses.size());
//
//        Category category = db.getCategory("category");
//        assertEquals(0, category.getCurrent());
//
//        List<Category> categories = db.getCategoriesWithTransactionSums(lastYearMs, nowMs);
//        assertEquals(1, categories.size());
//        assertEquals("category", categories.get(0).getName());
//        assertEquals(100 * (MONTHS_PER_YEAR + 1), categories.get(0).getMax());
//        assertEquals(expectedCurrent, categories.get(0).getCurrent());
//
//        final int NUM_REVENUES = 2000;
//
//        for (int index = 0; index < NUM_REVENUES; index++) {
//            result = db.insertTransaction("" + index, DatabaseManager.TransactionTable.REVENUE, "", "category", index, "", nowMs, "");
//            assertTrue(result);
//            expectedCurrent -= index;
//        }
//
//        List<Transaction> revenues = db.getRevenues();
//        assertEquals(NUM_REVENUES, revenues.size());
//
//        category = db.getCategory("category");
//        assertEquals(0, category.getCurrent());
//
//        // Account current value should be negative, as there is more
//        // revenue than expenses
//
//        categories = db.getCategoriesWithTransactionSums(lastYearMs, nowMs);
//        assertEquals(1, categories.size());
//        assertEquals("category", categories.get(0).getName());
//        assertEquals(100 * (MONTHS_PER_YEAR + 1), categories.get(0).getMax());
//        assertEquals(expectedCurrent, categories.get(0).getCurrent());
//
//        result = db.deleteCategory("category");
//        assertTrue(result);
//        assertEquals(db.getCategoryNames().size(), 0);
//        assertNull(db.getCategory("category"));
//
//        // Deleting the category does not delete the transactions
//        expenses = db.getExpenses();
//        assertEquals(NUM_EXPENSES, expenses.size());
//
//        revenues = db.getRevenues();
//        assertEquals(NUM_REVENUES, revenues.size());
//    }
//
//    @Test
//    public void multipleBudgets() {
//        final int NUM_BUDGETS = 1000;
//
//        // Add in reverse order to test sorting later
//        for (int index = NUM_BUDGETS; index > 0; index--) {
//            String name = String.format(Locale.getDefault(), "category%4d", index);
//            boolean result = db.insertCategory(name, index);
//            assertTrue(result);
//        }
//
//        assertEquals(NUM_BUDGETS, db.getCategoryCount());
//
//        List<Category> categories = db.getCategoriesWithTransactionSums(lastYearMs, nowMs);
//        int index = 1;
//        for (Category category : categories) {
//            assertEquals(category.getCurrent(), 0);
//            assertEquals(category.getMax(), index * (MONTHS_PER_YEAR + 1));
//            index++;
//        }
//
//        List<String> names = db.getCategoryNames();
//        index = 1;
//        for (String name : names) {
//            String expectedName = String.format(Locale.getDefault(), "category%4d", index);
//            assertEquals(expectedName, name);
//            index++;
//        }
//    }
//
//    @Test
//    public void updateBudget() {
//        boolean result = db.insertCategory("category", 100);
//        assertTrue(result);
//
//        for (int index = 0; index < 1000; index++) {
//            result = db.updateCategory("category", index);
//            assertTrue(result);
//            Category category = db.getCategory("category");
//            assertEquals(index, category.getMax());
//        }
//    }
//
//    @Test
//    public void updateMissingBudget() {
//        boolean result = db.updateCategory("category", 0);
//        assertEquals(false, result);
//
//    }
//
//    @Test
//    public void emptyBudgetValues() {
//        boolean result = db.insertCategory("", 0);
//        assertTrue(result);
//        assertEquals(1, db.getCategoryCount());
//
//        Category category = db.getCategory("");
//        assertEquals("", category.getName());
//        assertEquals(0, category.getMax());
//    }
//
//    private void checkTransaction(final Cursor cursor, final int type, final String description,
//                                  final String account, final String budget, final double value,
//                                  final String note, final long dateInMs, final String receipt) {
//        Transaction transaction = Transaction.toTransaction(cursor);
//        checkTransaction(transaction, type, description, account, budget, value, note, dateInMs,
//                receipt);
//    }
//
//    private void checkTransaction(final Transaction transaction, final int type, final String description,
//                                  final String account, final String budget, final double value,
//                                  final String note, final long dateInMs, final String receipt) {
//        assertEquals(transaction.getType(), type);
////        assertEquals(transaction.getDescription(), description);
//        assertEquals(transaction.getAccount(), account);
//        assertEquals(transaction.getCategory(), budget);
//        assertEquals(0, Double.compare(transaction.getValue(), value));
//        assertEquals(transaction.getNote(), note);
//        assertEquals(transaction.getDate(), dateInMs);
//        assertEquals(transaction.getReceipt(), receipt);
//    }

    @Test
    public void addRemoveOneTransaction() {
        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));

//        db.insertTransaction(DatabaseManager.TransactionTable.EXPENSE, "description", "account", "category", 100.50, "note", nowMs, "receipt");
//
//        assertEquals(1, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
//        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
//
//        List<Transaction> expenses = db.getExpenses();
//        for (Transaction t : expenses) {
//            checkTransaction(t, DatabaseManager.TransactionTable.EXPENSE, "description", "account", "category", 100.50, "note", nowMs, "receipt");
//        }
//
//
//        Transaction expenseTransaction = db.getTransaction(expenses.get(1).getName());
//        checkTransaction(expenseTransaction, DatabaseManager.TransactionTable.EXPENSE, "description", "account", "category", 100.50, "note", nowMs, "receipt");
//
//        db.deleteTransaction(expenses.get(1).getName());
//
//        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
//        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
//
//        db.insertTransaction(DatabaseManager.TransactionTable.REVENUE, "description2", "account2",
//                "budget2",
//                100.25, "note2", nowMs + 1, "receipt2");
//
//        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
//        assertEquals(1, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
//
//        List<Transaction> revenues = db.getRevenues();
//        for (Transaction t : revenues) {
//            checkTransaction(t, DatabaseManager.TransactionTable.REVENUE, "description2", "account2", "budget2", 100.25, "note2", nowMs + 1, "receipt2");
//        }
//
//
//        Transaction revenueTransaction = db.getTransaction(revenues.get(1).getName());
//        checkTransaction(revenueTransaction, DatabaseManager.TransactionTable.REVENUE, "description2", "account2",
//                "budget2", 100.25, "note2", nowMs + 1, "receipt2");
//
//        db.deleteTransaction(revenues.get(1).getName());
//
//        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
//        assertEquals(0, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
    }

    @Test
    public void multipleTransactions() {
//        final int NUM_TRANSACTIONS = 1000;
//        boolean result;
//
//        for (int type : new Integer[]{DatabaseManager.TransactionTable.REVENUE, DatabaseManager.TransactionTable.EXPENSE}) {
//            // Add in increasing order to test sorting later
//            for (int index = 1; index <= NUM_TRANSACTIONS; index++) {
//                result = db.insertTransaction(type, "", "", "", 0, "", index, "");
//                assertTrue(result);
//            }
//        }
//
//        assertEquals(NUM_TRANSACTIONS, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
//        assertEquals(NUM_TRANSACTIONS, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
//
//        for (Cursor cursor : new Cursor[]{db.getExpenses(), db.getRevenues()}) {
//            int index = NUM_TRANSACTIONS;
//            while (cursor.moveToNext()) {
//                Transaction transaction = Transaction.toTransaction(cursor);
//                assertEquals(index, transaction.getDate());
//                index--;
//            }
//            cursor.close();
//        }
    }

    @Test
    public void updateTransaction() {
//        boolean result = db.insertTransaction(DatabaseManager.TransactionTable.EXPENSE, "description", "account", "category", 100.50, "note", nowMs, "receipt");
//        assertTrue(result);
//        Transaction transaction = db.getTransaction(1);
//        checkTransaction(transaction, DatabaseManager.TransactionTable.EXPENSE, "description",
//                "account", "category", 100.50, "note", nowMs, "receipt");
//
//        result = db.updateTransaction(1, DatabaseManager.TransactionTable.EXPENSE, "description2",
//                "account2", "budget2", 25, "note2", nowMs + 1, "receipt2");
//        assertTrue(result);
//        transaction = db.getTransaction(1);
//        checkTransaction(transaction, DatabaseManager.TransactionTable.EXPENSE, "description2",
//                "account2", "budget2", 25, "note2", nowMs + 1, "receipt2");
    }

    @Test
    public void updateMissingTransaction() {
//        boolean result = db.updateTransaction(1, DatabaseManager.TransactionTable.EXPENSE, "", "", "", 0,
//                "", 0, "");
//        assertEquals(false, result);
    }

    private void setupDatabaseVersion1(SQLiteDatabase database) {
        // Delete the tables as they exist now
        database.execSQL("drop table " + DatabaseManager.CategoryTable.TABLE_NAME);
        database.execSQL("drop table " + DatabaseManager.TransactionTable.TABLE_NAME);

        // Create the table as it existed in revision 1
        database.execSQL(
                "create table  " + DatabaseManager.CategoryTable.TABLE_NAME + "(" +
                        DatabaseManager.CategoryTable.COL_NAME + " text primary key," +
                        DatabaseManager.CategoryTable.COL_MAX + " INTEGER not null)");
        database.execSQL("create table " + DatabaseManager.TransactionTable.TABLE_NAME + "(" +
                DatabaseManager.TransactionTable.COL_NAME + " INTEGER primary key autoincrement," +
                DatabaseManager.TransactionTable.COL_TYPE + " INTEGER not null," +
//                DatabaseManager.TransactionTable.COL_DESCRIPTION + " TEXT not null," +
                DatabaseManager.TransactionTable.COL_ACCOUNT + " TEXT," +
                DatabaseManager.TransactionTable.COL_CATEGORY + " TEXT," +
                DatabaseManager.TransactionTable.COL_VALUE + " REAL not null," +
                DatabaseManager.TransactionTable.COL_NOTE + " TEXT," +
                DatabaseManager.TransactionTable.COL_DATE + " INTEGER not null)");
    }

    private void insertBudgetAndTransactionVersion1(SQLiteDatabase database,
                                                    final String budgetName, final int budgetMax,
                                                    final int type, final String description,
                                                    final String account, final double value,
                                                    final String note, final long dateInMs) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseManager.CategoryTable.COL_NAME, budgetName);
        contentValues.put(DatabaseManager.CategoryTable.COL_MAX, budgetMax);
        long newId = database.insert(DatabaseManager.CategoryTable.TABLE_NAME, null, contentValues);
        assertTrue(newId != -1);

        contentValues = new ContentValues();
        contentValues.put(DatabaseManager.TransactionTable.COL_TYPE, type);
//        contentValues.put(DatabaseManager.TransactionTable.COL_DESCRIPTION, description);
        contentValues.put(DatabaseManager.TransactionTable.COL_ACCOUNT, account);
        contentValues.put(DatabaseManager.TransactionTable.COL_CATEGORY, budgetName);
        contentValues.put(DatabaseManager.TransactionTable.COL_VALUE, value);
        contentValues.put(DatabaseManager.TransactionTable.COL_NOTE, note);
        contentValues.put(DatabaseManager.TransactionTable.COL_DATE, dateInMs);
        newId = database.insert(DatabaseManager.TransactionTable.TABLE_NAME, null, contentValues);
        assertTrue(newId != -1);
    }

    @Test
    public void databaseUpgradeFromVersion1() {
        SQLiteDatabase database = db.getWritableDatabase();

        // Setup the database as it appeared in revision 1
        setupDatabaseVersion1(database);

        // Insert a category and transaction
        insertBudgetAndTransactionVersion1(database, "category", 100, DatabaseManager.TransactionTable.REVENUE,
                "description", "account", 1, "note", 200);

        // Upgrade database
        db.onUpgrade(database, DatabaseManager.ORIGINAL_DATABASE_VERSION, DatabaseManager.DATABASE_VERSION);

        // Determine that the entries are queryable and the fields are correct
        Category category = db.getCategory("category");
        assertEquals("category", category.getName());
        assertEquals(100, category.getMax());

//        Transaction transaction = db.getTransaction(1);
//        assertEquals(DatabaseManager.TransactionTable.REVENUE, transaction.getType());
//        assertEquals("description", transaction.getDescription());
//        assertEquals("account", transaction.getAccount());
//        assertEquals("category", transaction.getCategory());
//        assertEquals(0, Double.compare(1, transaction.getValue()));
//        assertEquals("note", transaction.getNote());
//        assertEquals(200, transaction.getDate());
//        assertEquals("", transaction.getReceipt());

        database.close();
    }

    @Test
    public void queryTransactionsWithReceipts() {
//        final int NUM_TRANSACTIONS_PER_TYPE = 1000;
//
//        for (int index = 0; index < NUM_TRANSACTIONS_PER_TYPE; index++) {
//            for (int type : new int[]{DatabaseManager.TransactionTable.EXPENSE, DatabaseManager.TransactionTable.REVENUE}) {
//                for (boolean hasReceipt : new boolean[]{true, false}) {
//                    String receipt = hasReceipt ? "receipt" : "";
//                    db.insertTransaction("" + index, type, "description", "account", "category", 0, "note", index, receipt);
//                }
//            }
//        }
//
//        assertEquals(NUM_TRANSACTIONS_PER_TYPE * 2, db.getTransactionCount(DatabaseManager.TransactionTable.EXPENSE));
//        assertEquals(NUM_TRANSACTIONS_PER_TYPE * 2, db.getTransactionCount(DatabaseManager.TransactionTable.REVENUE));
//
//        // There are 1000 * 2 * 2 transactions, half of which have a receipt.
//        // Check that only those with receipts are queried
//        final Long dateCutoffValue = (long) 250;
//        Cursor receiptTransactions = db.getTransactions(dateCutoffValue);
//
//        // There are 2000 transactions with a receipt. A cutoff of 250 will return
//        // the first quarter + 2 for the half way point.
//        assertEquals(NUM_TRANSACTIONS_PER_TYPE / 2 + 2, receiptTransactions.getCount());
//
//        while (receiptTransactions.moveToNext()) {
//            Transaction transaction = Transaction.toTransaction(receiptTransactions);
//            assertEquals("receipt", transaction.getReceipt());
//            assertTrue(transaction.getDate() <= dateCutoffValue);
//        }
//
//        receiptTransactions.close();
//
//        // Now ensure that all receipt transactions will be found if no cutoff
//        // date is provided
//
//        receiptTransactions = db.getTransactions(null);
//
//        // There are 2000 transactions with a receipt.
//        assertEquals(NUM_TRANSACTIONS_PER_TYPE * 2, receiptTransactions.getCount());
//
//        while (receiptTransactions.moveToNext()) {
//            Transaction transaction = Transaction.toTransaction(receiptTransactions);
//            assertEquals("receipt", transaction.getReceipt());
//        }
    }

    @Test
    public void filterTransactionsByBudget() {
//        boolean result;
//
//        final String BUDGET_1 = "budget1";
//        final String BUDGET_2 = "budget2";
//
//        for (String budget : new String[]{BUDGET_1, BUDGET_2}) {
//            result = db.insertCategory(budget, 100);
//            assertTrue(result);
//        }
//
//        final int NUM_TRANSACTIONS_BUDGET_1 = 10;
//        for (int index = 0; index < NUM_TRANSACTIONS_BUDGET_1; index++) {
//            for (int type : new int[]{DatabaseManager.TransactionTable.EXPENSE, DatabaseManager.TransactionTable.REVENUE}) {
//                result = db.insertTransaction("" + index, type, "description", "account", BUDGET_1, 0, "note", index, "");
//                assertTrue(result);
//            }
//        }
//
//        final int NUM_TRANSACTIONS_BUDGET_2 = 50;
//        for (int index = 0; index < NUM_TRANSACTIONS_BUDGET_2; index++) {
//            for (int type : new int[]{DatabaseManager.TransactionTable.EXPENSE, DatabaseManager.TransactionTable.REVENUE}) {
//                result = db.insertTransaction("" + index, type, "description", "account", BUDGET_2, 0, "note", index, "");
//                assertTrue(result);
//            }
//        }
//
//        List<Transaction> transactions = db.getExpensesForCategory(BUDGET_1);
//        assertEquals(NUM_TRANSACTIONS_BUDGET_1, transactions.size());
//
//        transactions = db.getRevenuesForCategory(BUDGET_1);
//        assertEquals(NUM_TRANSACTIONS_BUDGET_1, transactions.size());
//
//        transactions = db.getExpensesForCategory(BUDGET_2);
//        assertEquals(NUM_TRANSACTIONS_BUDGET_2, transactions.size());
//
//        transactions = db.getRevenuesForCategory(BUDGET_2);
//        assertEquals(NUM_TRANSACTIONS_BUDGET_2, transactions.size());
    }
}
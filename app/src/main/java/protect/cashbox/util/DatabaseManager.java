package protect.cashbox.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import protect.cashbox.account.Account;
import protect.cashbox.category.Category;
import protect.cashbox.transaction.Transaction;

public class DatabaseManager extends SQLiteOpenHelper {

    private static DatabaseManager INSTANCE;
    private static String DATABASE_NAME = "Cashbox.db";
    public static int ORIGINAL_DATABASE_VERSION = 1;
    public static int DATABASE_VERSION = 1;

    private String password;

    private DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (INSTANCE == null) {
            Log.e(Constants.TAG, "Creating new DB instance");
            INSTANCE = new DatabaseManager(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + AccountTable.TABLE_NAME + "(" +
                        AccountTable.COL_NAME + " TEXT PRIMARY KEY," +
                        AccountTable.COL_TYPE + " INTEGER NOT NULL," +
                        AccountTable.COL_OPENING_BALANCE + " REAL NOT NULL," +
                        AccountTable.COL_CURRENCY + " INTEGER NOT NULL," +
                        AccountTable.COL_COLOR + " INTEGER NOT NULL" +
                        ")");

        db.execSQL(
                "CREATE TABLE " + CategoryTable.TABLE_NAME + "(" +
                        CategoryTable.COL_NAME + " TEXT PRIMARY KEY," +
                        CategoryTable.COL_MAX + " INTEGER NOT NULL" +
                        ")");

        db.execSQL(
                "CREATE TABLE " + TransactionTable.TABLE_NAME + "(" +
                        TransactionTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        TransactionTable.COL_NAME + " TEXT NOT NULL," +
                        TransactionTable.COL_TYPE + " INTEGER NOT NULL," +
                        TransactionTable.COL_ACCOUNT + " TEXT," +
                        TransactionTable.COL_CATEGORY + " TEXT," +
                        TransactionTable.COL_VALUE + " REAL NOT NULL," +
                        TransactionTable.COL_NOTE + " TEXT," +
                        TransactionTable.COL_DATE + " INTEGER NOT NULL," +
                        TransactionTable.COL_RECEIPT + " TEXT" +
                        ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //NO UPGRADES UNTIL NOW
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(password.toCharArray());
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(password.toCharArray());
    }

    public boolean insertAccount(Account account) {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = insertAccount(db, account);
        db.close();
        return result;
    }

    public boolean insertAccount(SQLiteDatabase dbTransaction, Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountTable.COL_NAME, account.getName());
        values.put(AccountTable.COL_TYPE, account.getType());
        values.put(AccountTable.COL_OPENING_BALANCE, account.getOpeningBalance());
        values.put(AccountTable.COL_CURRENCY, account.getCurrency());
        values.put(AccountTable.COL_COLOR, account.getColor());

        try {
            dbTransaction.insertOrThrow(AccountTable.TABLE_NAME, null, values);
            Log.i(Constants.TAG, "Account created successfully: " + account.getName());
            return true;
        } catch (SQLException e) {
            Log.i(Constants.TAG, "Failed to create account: " + account.getName());
            return false;
        }
    }

    public boolean updateAccount(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountTable.COL_TYPE, account.getType());
        values.put(AccountTable.COL_OPENING_BALANCE, account.getOpeningBalance());
        values.put(AccountTable.COL_CURRENCY, account.getCurrency());
        values.put(AccountTable.COL_COLOR, account.getColor());

        SQLiteDatabase db = getWritableDatabase();
        int affectedRows = db.update(AccountTable.TABLE_NAME, values, AccountTable.COL_NAME + "=?", new String[]{account.getName()});
        db.close();

        if (affectedRows == 1) {
            Log.i(Constants.TAG, "Account updated successfully: " + account.getName());
            return true;
        } else {
            Log.i(Constants.TAG, "Failed to update account: " + account.getName());
            return false;
        }
    }

    public boolean deleteAccount(String name) {
        SQLiteDatabase db = getWritableDatabase();
        int affectedRows = db.delete(AccountTable.TABLE_NAME, AccountTable.COL_NAME + " = ? ", new String[]{name});
        db.close();

        if (affectedRows == 1) {
            Log.i(Constants.TAG, "Account deleted successfully: " + name);
            return true;
        } else {
            Log.i(Constants.TAG, "Failed to delete account: " + name);
            return false;
        }
    }

    public Account getAccount(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + AccountTable.TABLE_NAME + " WHERE " + AccountTable.COL_NAME + "=?", new String[]{name});

        Account account = null;
        if (data.getCount() == 1) {
            data.moveToFirst();
            account = new Account();
            account.setName(data.getString(data.getColumnIndexOrThrow(AccountTable.COL_NAME)));
            account.setType(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_TYPE)));
            account.setOpeningBalance(data.getFloat(data.getColumnIndexOrThrow(AccountTable.COL_OPENING_BALANCE)));
            account.setCurrency(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_CURRENCY)));
            account.setColor(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_COLOR)));

        }
        data.close();
        db.close();
        return account;
    }

    public List<Account> getAccounts() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor data = db.rawQuery("SELECT * FROM " + AccountTable.TABLE_NAME, null);

        ArrayList<Account> accounts = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Account a = new Account();
                a.setName(data.getString(data.getColumnIndexOrThrow(AccountTable.COL_NAME)));
                a.setType(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_TYPE)));
                a.setOpeningBalance(data.getFloat(data.getColumnIndexOrThrow(AccountTable.COL_OPENING_BALANCE)));
                a.setCurrency(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_CURRENCY)));
                a.setColor(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_COLOR)));
                accounts.add(a);
            } while (data.moveToNext());
        }
        data.close();
        db.close();

        return accounts;
    }

    public List<Account> getAccountsWithTransactionSums(long from, long to) {
        SQLiteDatabase db = getReadableDatabase();

        String TOTAL_EXPENSE = "total_expense";
        String TOTAL_REVENUE = "total_revenue";

        String ACCOUNT_ID = AccountTable.TABLE_NAME + "." + AccountTable.COL_NAME;
        String ACCOUNT_TYPE = AccountTable.TABLE_NAME + "." + AccountTable.COL_TYPE;
        String ACCOUNT_OPENING_BALANCE = AccountTable.TABLE_NAME + "." + AccountTable.COL_OPENING_BALANCE;
        String ACCOUNT_CURRENCY = AccountTable.TABLE_NAME + "." + AccountTable.COL_CURRENCY;
        String ACCOUNT_COLOR = AccountTable.TABLE_NAME + "." + AccountTable.COL_COLOR;
        String TRANSACTION_VALUE = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_VALUE;
        String TRANSACTION_TYPE = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_TYPE;
        String TRANSACTION_DATE = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_DATE;
        String TRANSACTION_ACCOUNT = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_ACCOUNT;

        Cursor data = db.rawQuery("SELECT " + ACCOUNT_ID + ", " + ACCOUNT_TYPE + ", " +
                        ACCOUNT_OPENING_BALANCE + ", " + ACCOUNT_CURRENCY + ", " + ACCOUNT_COLOR + ", " +
                        "(SELECT TOTAL(" + TRANSACTION_VALUE + ") FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                        ACCOUNT_ID + " = " + TRANSACTION_ACCOUNT + " AND " +
                        TRANSACTION_TYPE + " = ? AND " +
                        TRANSACTION_DATE + " >= ? AND " +
                        TRANSACTION_DATE + " <= ?) " +
                        "AS " + TOTAL_EXPENSE + ", " +
                        "(SELECT TOTAL(" + TRANSACTION_VALUE + ") FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                        ACCOUNT_ID + " = " + TRANSACTION_ACCOUNT + " AND " +
                        TRANSACTION_TYPE + " = ? AND " +
                        TRANSACTION_DATE + " >= ? AND " +
                        TRANSACTION_DATE + " <= ?) " +
                        "AS " + TOTAL_REVENUE + " " +
                        "FROM " + AccountTable.TABLE_NAME + " ORDER BY " + ACCOUNT_ID,
                new String[]{
                        Integer.toString(TransactionTable.EXPENSE),
                        Long.toString(from),
                        Long.toString(to),
                        Integer.toString(TransactionTable.REVENUE),
                        Long.toString(from),
                        Long.toString(to)
                });

        ArrayList<Account> accounts = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Account a = new Account();
                a.setName(data.getString(data.getColumnIndexOrThrow(AccountTable.COL_NAME)));
                a.setType(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_TYPE)));
                a.setExpenses(data.getDouble(data.getColumnIndexOrThrow(TOTAL_EXPENSE)));
                a.setIncomes(data.getDouble(data.getColumnIndexOrThrow(TOTAL_REVENUE)));
                a.setOpeningBalance(data.getFloat(data.getColumnIndexOrThrow(AccountTable.COL_OPENING_BALANCE)));
                a.setCurrency(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_CURRENCY)));
                a.setColor(data.getInt(data.getColumnIndexOrThrow(AccountTable.COL_COLOR)));
                accounts.add(a);
            } while (data.moveToNext());
        }
        data.close();
        db.close();

        return accounts;
    }

    public List<String> getAccountNames() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT " + AccountTable.COL_NAME + " FROM " + AccountTable.TABLE_NAME + " ORDER BY " + AccountTable.COL_NAME, null);

        LinkedList<String> names = new LinkedList<>();
        if (data.moveToFirst()) {
            do {
                String name = data.getString(data.getColumnIndexOrThrow(AccountTable.COL_NAME));
                names.add(name);
            } while (data.moveToNext());
        }
        data.close();
        db.close();

        return names;
    }

    public int getAccountCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT COUNT(*) FROM " + AccountTable.TABLE_NAME, null);

        int count = 0;
        if (data.getCount() == 1) {
            data.moveToFirst();
            count = data.getInt(0);
        }
        data.close();
        db.close();

        return count;
    }

    public boolean insertCategory(Category category) {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = insertCategory(db, category);
        db.close();
        return result;
    }

    //PROVIDED DB INSTANCE TO MANUALLY CONTROL DB TRANSACTION
    public boolean insertCategory(SQLiteDatabase dbTransaction, Category category) {
        ContentValues values = new ContentValues();
        values.put(CategoryTable.COL_NAME, category.getName());
        values.put(CategoryTable.COL_MAX, category.getMax());

        try {
            dbTransaction.insertOrThrow(CategoryTable.TABLE_NAME, null, values);
            Log.i(Constants.TAG, "Category created successfully: " + category.getName());
            return true;
        } catch (SQLException e) {
            Log.i(Constants.TAG, "Failed to create category: " + category.getName());
            return false;
        }
    }

    public boolean updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(CategoryTable.COL_MAX, category.getMax());

        SQLiteDatabase db = getWritableDatabase();
        int affectedRows = db.update(CategoryTable.TABLE_NAME, values, CategoryTable.COL_NAME + "=?", new String[]{category.getName()});
        db.close();

        if (affectedRows == 1) {
            Log.i(Constants.TAG, "Category updated successfully: " + category.getName());
            return true;
        } else {
            Log.i(Constants.TAG, "Failed to update category: " + category.getName());
            return false;
        }
    }

    public boolean deleteCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        int affectedRows = db.delete(CategoryTable.TABLE_NAME, CategoryTable.COL_NAME + " = ? ", new String[]{name});
        db.close();

        if (affectedRows == 1) {
            Log.i(Constants.TAG, "Category deleted successfully: " + name);
            return true;
        } else {
            Log.i(Constants.TAG, "Failed to delete category: " + name);
            return false;
        }
    }

    public Category getCategory(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME + " WHERE " + CategoryTable.COL_NAME + "=?", new String[]{name});

        Category category = null;
        if (data.getCount() == 1) {
            data.moveToFirst();
            category = new Category();
            category.setName(data.getString(data.getColumnIndexOrThrow(CategoryTable.COL_NAME)));
            category.setMax(data.getInt(data.getColumnIndexOrThrow(CategoryTable.COL_MAX)));
        }
        data.close();
        db.close();
        return category;
    }

    public List<Category> getCategories() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + CategoryTable.TABLE_NAME, null);

        ArrayList<Category> categories = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                String name = data.getString(data.getColumnIndexOrThrow(CategoryTable.COL_NAME));
                int max = data.getInt(data.getColumnIndexOrThrow(CategoryTable.COL_MAX));
                categories.add(new Category(name, max, 0));
            } while (data.moveToNext());
        }
        data.close();
        db.close();

        return categories;
    }

    public List<Category> getCategoriesWithTransactionSums(long from, long to) {
        SQLiteDatabase db = getReadableDatabase();

        String TOTAL_EXPENSE_COL = "total_expense";
        String TOTAL_REVENUE_COL = "total_revenue";

        String CATEGORY_ID = CategoryTable.TABLE_NAME + "." + CategoryTable.COL_NAME;
        String CATEGORY_MAX = CategoryTable.TABLE_NAME + "." + CategoryTable.COL_MAX;
        String TRANS_VALUE = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_VALUE;
        String TRANS_TYPE = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_TYPE;
        String TRANS_DATE = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_DATE;
        String TRANS_CATEGORY = TransactionTable.TABLE_NAME + "." + TransactionTable.COL_CATEGORY;

        Cursor data = db.rawQuery("SELECT " + CATEGORY_ID + ", " + CATEGORY_MAX + ", " +
                        "(SELECT TOTAL(" + TRANS_VALUE + ") FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                        CATEGORY_ID + " = " + TRANS_CATEGORY + " AND " +
                        TRANS_TYPE + " = ? AND " +
                        TRANS_DATE + " >= ? AND " +
                        TRANS_DATE + " <= ?) " +
                        "AS " + TOTAL_EXPENSE_COL + ", " +
                        "(SELECT TOTAL(" + TRANS_VALUE + ") FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                        CATEGORY_ID + " = " + TRANS_CATEGORY + " AND " +
                        TRANS_TYPE + " = ? AND " +
                        TRANS_DATE + " >= ? AND " +
                        TRANS_DATE + " <= ?) " +
                        "AS " + TOTAL_REVENUE_COL + " " +
                        "FROM " + CategoryTable.TABLE_NAME + " ORDER BY " + CATEGORY_ID,
                new String[]
                        {
                                Integer.toString(TransactionTable.EXPENSE),
                                Long.toString(from),
                                Long.toString(to),
                                Integer.toString(TransactionTable.REVENUE),
                                Long.toString(from),
                                Long.toString(to)
                        });

        LinkedList<Category> categories = new LinkedList<>();

        if (data.moveToFirst()) {
            do {
                String name = data.getString(data.getColumnIndexOrThrow(CategoryTable.COL_NAME));
                int max = data.getInt(data.getColumnIndexOrThrow(CategoryTable.COL_MAX));
                int expenses = data.getInt(data.getColumnIndexOrThrow(TOTAL_EXPENSE_COL));
                int revenues = data.getInt(data.getColumnIndexOrThrow(TOTAL_REVENUE_COL));
                int current = expenses - revenues;
                categories.add(new Category(name, max, current));
            } while (data.moveToNext());
        }
        data.close();
        db.close();

        return categories;
    }

    public List<String> getCategoryNames() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT " + CategoryTable.COL_NAME + " FROM " + CategoryTable.TABLE_NAME + " ORDER BY " + CategoryTable.COL_NAME, null);

        LinkedList<String> names = new LinkedList<>();
        if (data.moveToFirst()) {
            do {
                String name = data.getString(data.getColumnIndexOrThrow(CategoryTable.COL_NAME));
                names.add(name);
            } while (data.moveToNext());
        }
        data.close();
        db.close();

        return names;
    }

    public int getCategoryCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT COUNT(*) FROM " + CategoryTable.TABLE_NAME, null);

        int count = 0;
        if (data.getCount() == 1) {
            data.moveToFirst();
            count = data.getInt(0);
        }
        data.close();
        db.close();

        return count;
    }

    public boolean insertTransaction(Transaction transaction) {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = insertTransaction(db, transaction);
        db.close();
        return result;
    }

    public boolean insertTransaction(SQLiteDatabase dbTransaction, Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(TransactionTable.COL_NAME, transaction.getName());
        values.put(TransactionTable.COL_TYPE, transaction.getType());
        values.put(TransactionTable.COL_ACCOUNT, transaction.getAccount());
        values.put(TransactionTable.COL_CATEGORY, transaction.getCategory());
        values.put(TransactionTable.COL_VALUE, transaction.getValue());
        values.put(TransactionTable.COL_NOTE, transaction.getNote());
        values.put(TransactionTable.COL_DATE, transaction.getDate());
        values.put(TransactionTable.COL_RECEIPT, transaction.getReceipt());

        try {
            long id = dbTransaction.insertOrThrow(TransactionTable.TABLE_NAME, null, values);
            Log.i(Constants.TAG, "Transaction created successfully: " + id);
            return true;
        } catch (SQLException e) {
            Log.i(Constants.TAG, "Failed to create transaction: " + transaction.getId());
            return false;
        }
    }

    public boolean updateTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(TransactionTable.COL_NAME, transaction.getName());
        values.put(TransactionTable.COL_TYPE, transaction.getType());
        values.put(TransactionTable.COL_ACCOUNT, transaction.getAccount());
        values.put(TransactionTable.COL_CATEGORY, transaction.getCategory());
        values.put(TransactionTable.COL_VALUE, transaction.getValue());
        values.put(TransactionTable.COL_NOTE, transaction.getNote());
        values.put(TransactionTable.COL_DATE, transaction.getDate());
        values.put(TransactionTable.COL_RECEIPT, transaction.getReceipt());

        SQLiteDatabase db = getWritableDatabase();
        int affectedRows = db.update(TransactionTable.TABLE_NAME, values, TransactionTable.COL_ID + "=?", new String[]{String.valueOf(transaction.getId())});
        db.close();

        if (affectedRows == 1) {
            Log.i(Constants.TAG, "Transaction updated successfully: " + transaction.getId());
            return true;
        } else {
            Log.i(Constants.TAG, "Failed to update transaction: " + transaction.getId());
            return false;
        }
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int affectedRows = db.delete(TransactionTable.TABLE_NAME, TransactionTable.COL_ID + " = ? ", new String[]{String.valueOf(id)});
        db.close();

        if (affectedRows == 1) {
            Log.i(Constants.TAG, "Transaction deleted successfully: " + id);
            return true;
        } else {
            Log.i(Constants.TAG, "Failed to deleted transaction: " + id);
            return false;
        }
    }

    public Transaction getTransaction(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE " + TransactionTable.COL_ID + "=?", new String[]{String.valueOf(id)});

        Transaction t = null;
        if (data.getCount() == 1) {
            data.moveToFirst();
            t = new Transaction();
            t.setId(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_ID)));
            t.setType(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_TYPE)));
            t.setName(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NAME)));
            t.setAccount(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_ACCOUNT)));
            t.setCategory(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_CATEGORY)));
            t.setValue(data.getDouble(data.getColumnIndexOrThrow(TransactionTable.COL_VALUE)));
            t.setNote(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NOTE)));
            t.setDate(data.getLong(data.getColumnIndexOrThrow(TransactionTable.COL_DATE)));
            t.setReceipt(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_RECEIPT)));
        }
        data.close();
        db.close();

        return t;
    }

    public List<Transaction> getTransactions(Long until) {
        List<String> argList = new ArrayList<>();
        if (until != null) {
            argList.add(until.toString());
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                        " LENGTH(" + TransactionTable.COL_RECEIPT + ") > 0 " +
                        (until != null ? " AND " + TransactionTable.COL_DATE + "<=? " : ""),
                argList.toArray(new String[argList.size()]));
        db.close();

        List<Transaction> transactions = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_ID)));
                t.setName(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NAME)));
                t.setType(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_TYPE)));
                t.setAccount(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_ACCOUNT)));
                t.setCategory(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_CATEGORY)));
                t.setValue(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_VALUE)));
                t.setNote(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NOTE)));
                t.setDate(data.getLong(data.getColumnIndexOrThrow(TransactionTable.COL_DATE)));
                t.setReceipt(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_RECEIPT)));
                transactions.add(t);
            } while (data.moveToNext());
        }
        data.close();
        db.close();
        return transactions;
    }

    public int getTransactionCount(int type) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT COUNT(*) FROM " + TransactionTable.TABLE_NAME +
                " WHERE " + TransactionTable.COL_TYPE + "=?", new String[]{Integer.toString(type)});

        int count = 0;
        if (data.getCount() == 1) {
            data.moveToFirst();
            count = data.getInt(0);
        }

        data.close();
        db.close();
        return count;
    }

    public List<Transaction> getExpenses() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                TransactionTable.COL_TYPE + "=" + TransactionTable.EXPENSE +
                " ORDER BY " + TransactionTable.COL_DATE + " DESC", null);

        List<Transaction> transactions = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_ID)));
                t.setName(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NAME)));
                t.setType(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_TYPE)));
                t.setAccount(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_ACCOUNT)));
                t.setCategory(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_CATEGORY)));
                t.setValue(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_VALUE)));
                t.setNote(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NOTE)));
                t.setDate(data.getLong(data.getColumnIndexOrThrow(TransactionTable.COL_DATE)));
                t.setReceipt(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_RECEIPT)));
                transactions.add(t);
            } while (data.moveToNext());
        }
        data.close();
        db.close();
        return transactions;
    }

    public List<Transaction> getExpensesForCategory(String category, long filterFrom, long filterUntil) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                TransactionTable.COL_TYPE + "=" + TransactionTable.EXPENSE +
                " AND " + TransactionTable.COL_CATEGORY + " = ?" +
                " AND " + TransactionTable.COL_DATE + " >= ?" +
                " AND " + TransactionTable.COL_DATE + " <= ?" +
                " ORDER BY " + TransactionTable.COL_DATE + " DESC", new String[]{category, String.valueOf(filterFrom), String.valueOf(filterUntil)});

        List<Transaction> transactions = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_ID)));
                t.setName(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NAME)));
                t.setType(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_TYPE)));
                t.setAccount(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_ACCOUNT)));
                t.setCategory(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_CATEGORY)));
                t.setValue(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_VALUE)));
                t.setNote(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NOTE)));
                t.setDate(data.getLong(data.getColumnIndexOrThrow(TransactionTable.COL_DATE)));
                t.setReceipt(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_RECEIPT)));
                transactions.add(t);
            } while (data.moveToNext());
        }
        data.close();
        db.close();
        return transactions;
    }

    public List<Transaction> getRevenues() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                TransactionTable.COL_TYPE + "=" + TransactionTable.REVENUE +
                " ORDER BY " + TransactionTable.COL_DATE + " DESC", null);

        List<Transaction> transactions = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_ID)));
                t.setName(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NAME)));
                t.setType(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_TYPE)));
                t.setAccount(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_ACCOUNT)));
                t.setCategory(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_CATEGORY)));
                t.setValue(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_VALUE)));
                t.setNote(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NOTE)));
                t.setDate(data.getLong(data.getColumnIndexOrThrow(TransactionTable.COL_DATE)));
                t.setReceipt(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_RECEIPT)));
                transactions.add(t);
            } while (data.moveToNext());
        }
        data.close();
        db.close();
        return transactions;
    }

    public List<Transaction> getRevenuesForCategory(String category) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE " +
                TransactionTable.COL_TYPE + "=" + TransactionTable.REVENUE +
                " AND " + TransactionTable.COL_CATEGORY + "=?" +
                " ORDER BY " + TransactionTable.COL_DATE + " DESC", new String[]{category});

        List<Transaction> transactions = new ArrayList<>();
        if (data.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_ID)));
                t.setName(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NAME)));
                t.setType(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_TYPE)));
                t.setAccount(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_ACCOUNT)));
                t.setCategory(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_CATEGORY)));
                t.setValue(data.getInt(data.getColumnIndexOrThrow(TransactionTable.COL_VALUE)));
                t.setNote(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_NOTE)));
                t.setDate(data.getLong(data.getColumnIndexOrThrow(TransactionTable.COL_DATE)));
                t.setReceipt(data.getString(data.getColumnIndexOrThrow(TransactionTable.COL_RECEIPT)));
                transactions.add(t);
            } while (data.moveToNext());
        }
        data.close();
        db.close();
        return transactions;
    }

    public boolean unlock(String password) {
        try {
            getReadableDatabase(password).close();
            this.password = password;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void lock() {
        this.password = null;
    }

    public static class AccountTable {
        public static String TABLE_NAME = "accounts";
        public static String COL_NAME = "_id";
        public static String COL_TYPE = "type";
        public static String COL_OPENING_BALANCE = "balance";
        public static String COL_CURRENCY = "currency";
        public static String COL_COLOR = "color";

    }

    public static class CategoryTable {
        public static String TABLE_NAME = "categories";
        public static String COL_NAME = "_id";
        public static String COL_MAX = "max";
    }

    public static class TransactionTable {
        public static String TABLE_NAME = "transactions";
        public static String COL_ID = "_id";
        public static String COL_TYPE = "type";
        public static String COL_NAME = "name";
        public static String COL_ACCOUNT = "account";
        public static String COL_CATEGORY = "category";
        public static String COL_VALUE = "value";
        public static String COL_NOTE = "note";
        public static String COL_DATE = "date";
        public static String COL_RECEIPT = "receipt";

        public static int EXPENSE = 1;
        public static int REVENUE = 2;
    }
}

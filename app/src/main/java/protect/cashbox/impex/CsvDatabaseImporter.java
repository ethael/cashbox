package protect.cashbox.impex;

import net.sqlcipher.database.SQLiteDatabase;
import android.util.Log;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;

import protect.cashbox.account.Account;
import protect.cashbox.category.Category;
import protect.cashbox.transaction.Transaction;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.ParseException;
import protect.cashbox.util.DatabaseManager.TransactionTable;

public class CsvDatabaseImporter {

    public void importData(DatabaseManager db, InputStreamReader input) throws IOException, ParseException, InterruptedException {
        final CSVParser parser = new CSVParser(input, CSVFormat.RFC4180.withHeader());

        SQLiteDatabase dbTransaction = db.getWritableDatabase();
        dbTransaction.beginTransaction();

        try {
            for (CSVRecord record : parser) {
                switch (record.get(0)) {
                    case Constants.ACCOUNT:
                        importAccount(dbTransaction, db, record);
                        break;
                    case Constants.CATEGORY:
                        importCategory(dbTransaction, db, record);
                        break;
                    case Constants.TRANSACTION:
                        importTransaction(dbTransaction, db, record);
                        break;
                }
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }

            parser.close();
            dbTransaction.setTransactionSuccessful();
        } catch (IllegalArgumentException e) {
            throw new ParseException("Failed to import data from import file: " + e.getMessage(), e);
        } finally {
            dbTransaction.endTransaction();
            dbTransaction.close();
        }
    }

    private void importAccount(SQLiteDatabase dbTransaction, DatabaseManager manager, CSVRecord record) {
        Account a = new Account();
        a.setName(record.get(1));
        a.setType(Integer.valueOf(record.get(2)));
        a.setOpeningBalance(Double.valueOf(record.get(3)));
        a.setCurrency(Integer.valueOf(record.get(4)));
        a.setColor(Integer.valueOf(record.get(5)));

        manager.insertAccount(dbTransaction, a);
        Log.i(Constants.TAG, "Importing account: " + a);
    }

    private void importCategory(SQLiteDatabase dbTransaction, DatabaseManager manager, CSVRecord record) throws IOException, ParseException {
        Category c = new Category();
        c.setName(record.get(1));
        c.setMax(Integer.valueOf(record.get(2)));

        manager.insertCategory(dbTransaction, c);
        Log.i(Constants.TAG, "Importing category: " + c);
    }

    private void importTransaction(SQLiteDatabase dbTransaction, DatabaseManager manager, CSVRecord record) throws IOException, ParseException {
        Transaction t = new Transaction();
        t.setId(Integer.valueOf(record.get(1)));
        t.setName(record.get(2));
        t.setType(record.get(3).equals("EXPENSE") ? TransactionTable.EXPENSE : TransactionTable.REVENUE);
        t.setAccount(record.get(4));
        t.setCategory(record.get(5));
        t.setValue(Double.valueOf(record.get(6)));
        t.setNote(record.get(7));
        t.setDate(Long.valueOf(record.get(8)));
        manager.insertTransaction(dbTransaction, t);
        Log.i(Constants.TAG, "Importing transaction: " + t);
    }
}

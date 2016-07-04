package protect.cashbox.impex;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import protect.cashbox.account.Account;
import protect.cashbox.category.Category;
import protect.cashbox.transaction.Transaction;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;

public class CsvDatabaseExporter {

    public void exportData(DatabaseManager db, OutputStreamWriter output) throws IOException, InterruptedException {
        CSVPrinter printer = new CSVPrinter(output, CSVFormat.RFC4180);
        Thread thread = Thread.currentThread();

        //HEADER
        printer.printRecord("OBJECT", "ID", "ATTR1", "ATTR2", "ATTR3", "ATTR4", "ATTR5", "ATTR6", "ATTR7");

        //ACCOUNTS
        for (String accountName : db.getAccountNames()) {
            Account account = db.getAccount(accountName);
            printer.printRecord(
                    Constants.ACCOUNT,
                    account.getName(),
                    account.getType(),
                    account.getOpeningBalance(),
                    account.getCurrency(),
                    account.getColor(),
                    "",
                    "",
                    ""
            );

            if (thread.isInterrupted()) {
                throw new InterruptedException();
            }
        }

        //CATEGORIES
        for (String categoryName : db.getCategoryNames()) {
            Category category = db.getCategory(categoryName);
            printer.printRecord(
                    Constants.CATEGORY,
                    category.getName(),
                    category.getMax(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            );

            if (thread.isInterrupted()) {
                throw new InterruptedException();
            }
        }

        //TRANSACTIONS
        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(db.getExpenses());
        transactions.addAll(db.getRevenues());
        for (Transaction t : transactions) {
            printer.printRecord(
                    Constants.TRANSACTION,
                    t.getId(),
                    t.getName(),
                    t.getType() == DatabaseManager.TransactionTable.EXPENSE ? "EXPENSE" : "REVENUE",
                    t.getAccount(),
                    t.getCategory(),
                    t.getValue(),
                    t.getNote(),
                    t.getDate());
            if (thread.isInterrupted()) {
                throw new InterruptedException();
            }
        }
        printer.close();
    }
}
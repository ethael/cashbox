package protect.cashbox.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import protect.cashbox.R;
import protect.cashbox.transaction.Transaction;

public class PurgeTask extends AsyncTask<Void, Void, Void> {

    private final Activity activity;
    private final Long purgeUntil;

    private ProgressDialog progress;

    public PurgeTask(Activity activity) {
        super();
        this.activity = activity;
        purgeUntil = null;
    }

    public PurgeTask(Activity activity, long purgeUntil) {
        super();
        this.activity = activity;
        this.purgeUntil = purgeUntil;
    }

    protected void onPreExecute() {
        progress = new ProgressDialog(activity);
        progress.setTitle(R.string.cleaning);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                PurgeTask.this.cancel(true);
            }
        });
        progress.show();
    }

    protected Void doInBackground(Void... nothing) {
        //PURGE ALL RECEIPTS UNTIL purgeUntil DATE
        DatabaseManager db = DatabaseManager.getInstance(activity);
        if (purgeUntil != null) {
            for (Transaction t : db.getTransactions(purgeUntil)) {
                if (t.getReceipt() != null) {
                    if (new File(t.getReceipt()).delete()) {
                        t.setReceipt(null);
                        db.updateTransaction(t);
                    } else {
                        Log.e(Constants.TAG, "Failed to delete old receipt from transaction: " + t.getName());
                    }
                }
            }
        }

        //CORRECT TRANSACTIONS WITH MISSING RECEIPTS
        for (Transaction t : db.getTransactions(null)) {
            if (t.getReceipt() != null) {
                if (!new File(t.getReceipt()).isFile()) {
                    t.setReceipt(null);
                    db.updateTransaction(t);
                    Log.w(Constants.TAG, "Transaction " + t.getId() + " listed a receipt but it is missing, removing receipt");
                }
            }
        }

        //DELETE ORPHANED RECEIPTS
        File imageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (imageDir == null || !imageDir.exists()) {
            return null;
        }

        File[] receipts = imageDir.listFiles();
        if (receipts == null) {
            return null;
        }

        for (File receipt : receipts) {
            boolean found = false;
            for (Transaction t : db.getTransactions(null)) {
                if (t.getReceipt() != null) {
                    if (new File(t.getReceipt()).equals(receipt)) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                Log.w(Constants.TAG, "Deleting orphaned receipt: " + receipt.getAbsolutePath());
                if (!receipt.delete()) {
                    Log.e(Constants.TAG, "Failed to delete orphaned receipt: " + receipt.getAbsolutePath());
                }
            }
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        progress.dismiss();
        Log.i(Constants.TAG, "Cleanup Complete");
    }

    protected void onCancelled() {
        progress.dismiss();
        Log.i(Constants.TAG, "Cleanup Cancelled");
    }
}

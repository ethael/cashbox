package protect.cashbox.impex;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;
import protect.cashbox.util.ParseException;

public class ImportTask extends AsyncTask<Void, Void, Void> {

    private static final String TARGET_FILE = "Cashbox.csv";

    private Activity activity;
    private ProgressDialog progress;

    public ImportTask(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(activity);
        progress.setTitle(R.string.importing);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ImportTask.this.cancel(true);
            }
        });
        progress.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        progress.dismiss();
        Log.i(Constants.TAG, "Import completed");
    }

    @Override
    protected void onCancelled() {
        progress.dismiss();
        Log.i(Constants.TAG, "Import cancelled");
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        final File sdcardDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        final File importFile = new File(sdcardDir, TARGET_FILE);
        final DatabaseManager db = DatabaseManager.getInstance(activity);

        if (!importFile.exists()) {
            toast(R.string.fileMissing, importFile.getAbsolutePath());
        } else {
            try {
                FileInputStream fileReader = new FileInputStream(importFile);
                InputStreamReader reader = new InputStreamReader(fileReader, Charset.forName("UTF-8"));
                try {
                    new CsvDatabaseImporter().importData(db, reader);
                    toast(R.string.importedFrom, importFile.getAbsolutePath());
                } catch (IOException | ParseException | InterruptedException e) {
                    Log.e(Constants.TAG, "Failed to input data", e);
                    toast(R.string.importFailed, importFile.getAbsolutePath());
                } finally {
                    reader.close();
                    fileReader.close();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "Failed to input data", e);
                toast(R.string.importFailed, importFile.getAbsolutePath());
            }
        }
        return null;
    }

    private void toast(int stringId, String argument) {
        final String template = activity.getResources().getString(stringId);
        final String message = String.format(template, argument);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

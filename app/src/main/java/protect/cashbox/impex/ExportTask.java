package protect.cashbox.impex;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.DatabaseManager;

public class ExportTask extends AsyncTask<Void, Void, Void> {

    private static final String TARGET_FILE = "Cashbox.csv";

    private Activity activity;
    private ProgressDialog progress;

    public ExportTask(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(activity);
        progress.setTitle(R.string.exporting);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ExportTask.this.cancel(true);
            }
        });
        progress.show();
    }

    @Override
    protected void onPostExecute(Void result) {
        progress.dismiss();
        Log.i(Constants.TAG, "Export completed");
    }

    @Override
    protected void onCancelled() {
        progress.dismiss();
        Log.i(Constants.TAG, "Export cancelled");
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        final File sdcardDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        final File exportFile = new File(sdcardDir, TARGET_FILE);
        final DatabaseManager db = DatabaseManager.getInstance(activity);

        if (!exportFile.exists()) {
            toast(R.string.fileMissing, exportFile.getAbsolutePath());
        } else {
            try {
                FileOutputStream fileWriter = new FileOutputStream(exportFile);
                OutputStreamWriter writer = new OutputStreamWriter(fileWriter, Charset.forName("UTF-8"));
                try {
                    new CsvDatabaseExporter().exportData(db, writer);
                    toast(R.string.exportedTo, exportFile.getAbsolutePath());
                } catch (IOException | InterruptedException e) {
                    Log.e(Constants.TAG, "Failed to input data", e);
                    toast(R.string.exportFailed, exportFile.getAbsolutePath());
                } finally {
                    writer.close();
                    fileWriter.close();
                }
            } catch (IOException e) {
                Log.e(Constants.TAG, "Unable to export file", e);
                toast(R.string.exportFailed, exportFile.getAbsolutePath());
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

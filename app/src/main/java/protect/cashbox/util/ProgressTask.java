package protect.cashbox.util;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public abstract class ProgressTask extends AsyncTask<Void, Void, Object> {

    private Activity activity;
//    private ProgressBar progress;

    public ProgressTask(Activity activity) {
        super();
        this.activity = activity;
//        this.progress = (ProgressBar) activity.findViewById(R.id.progressBar);
    }

    @Override
    protected void onPreExecute() {
//        progress.setVisibility(View.VISIBLE);
//        progress.setProgress(1);
    }

    @Override
    protected void onPostExecute(Object result) {
//        progress.setVisibility(View.GONE);
        Log.d(Constants.TAG, "ProgressTask completed");
        callback(result);
    }

    @Override
    protected void onCancelled() {
//        progress.setVisibility(View.GONE);
        Log.i(Constants.TAG, "ProgressTask cancelled");
    }

    @Override
    protected Object doInBackground(Void... nothing) {
        return process();
    }

    protected abstract Object process();

    protected abstract void callback(Object result);

    protected void toast(int id, final String message) {
        final String template = activity.getResources().getString(id);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, String.format(template, message), Toast.LENGTH_LONG).show();
            }
        });
    }
}

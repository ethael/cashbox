package protect.cashbox.transaction;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.io.File;

import protect.cashbox.CashboxActivity;
import protect.cashbox.R;
import protect.cashbox.util.Constants;
import protect.cashbox.util.ProgressTask;

public class ReceiptDetailActivity extends CashboxActivity {

    private String receiptFilename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_receipt_activity);

        //INIT TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        //INIT ACTION BAR
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //RENDER IMAGE FULLSCREEN
        receiptFilename = getIntent().getExtras().getString(Constants.EXTRAS_RECEIPT_FILE_NAME);
        final WebView view = (WebView) findViewById(R.id.imageView);
        view.getSettings().setBuiltInZoomControls(true);
        view.getSettings().setAllowFileAccess(true);
        String data = "<html><body><img width='100%' src='file://" + receiptFilename + "'/></body></html>";
        view.loadDataWithBaseURL("", data, "text/html", "utf-8", null);
    }

    @Override
    protected ProgressTask asyncTask() {
        //NO NEED FOR ASYNC LOAD
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (shareActionProvider == null) {
            Log.e(Constants.TAG, "Failed to find share action provider");
            return false;
        }

        if (receiptFilename == null) {
            Log.e(Constants.TAG, "No receipt to share");
            return false;
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(receiptFilename, opt);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(opt.outMimeType);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(receiptFilename)));
        shareActionProvider.setShareIntent(intent);

        return super.onCreateOptionsMenu(menu);
    }
}

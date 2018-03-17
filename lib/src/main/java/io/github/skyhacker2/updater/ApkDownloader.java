package io.github.skyhacker2.updater;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by eleven on 2018/3/16.
 */

public class ApkDownloader {
    private final static String TAG = ApkDownloader.class.getSimpleName();

    private Activity mContext;
    private DownloadManager mDownloadManager;
    private long mDownloadId;
    private String mDownloadPath;
    private boolean mDownloading;

    BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                mContext.getApplicationContext().unregisterReceiver(mDownloadReceiver);
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == mDownloadId) {
                    Log.d(TAG, "下载完成");
                    mDownloading = false;
                    Uri uri = checkIfAlreadyExist();
                    if (uri != null) {
                        Log.d(TAG, "uri " + uri);
                        installApk(uri);
                    }
                }
            }
        }
    };

    public ApkDownloader(Activity context) {
        mContext = context;
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void download(final String url, String fileName) {
        if (mDownloading) {
            return;
        }
        // 注册下载完成广播
        mDownloading = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        mContext.getApplicationContext().registerReceiver(mDownloadReceiver, filter);
        Toast.makeText(mContext, "开始下载", Toast.LENGTH_LONG).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(fileName);
        request = request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        long id = mDownloadManager.enqueue(request);
        mDownloadId = id;
        File folder = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS);
        File file = new File(folder, fileName);
        mDownloadPath = file.getAbsolutePath();
    }

    private void installApk(Uri uri) {
        if (uri != null) {
            Intent apkIntent = new Intent(Intent.ACTION_VIEW);
            apkIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "uri " + uri);
            apkIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            mContext.startActivity(apkIntent);
        }
    }

    private Uri checkIfAlreadyExist() {
        Log.d(TAG, "downloadPath " + mDownloadPath);
        if (mDownloadPath != null) {
            File apkFile = new File(mDownloadPath);
            if (apkFile.exists()) {
                return getFileUri(apkFile);
            } else {
                return null;
            }
        }
        return null;
    }

    private Uri getFileUri(File file) {
        Log.d(TAG, "authorities " + mContext.getPackageName() + ".fileprovider");
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", file);
        } else {
            return Uri.parse("file://" + file.getAbsolutePath());
        }
    }

    private String getString(String key) {
        int id = mContext.getResources().getIdentifier(key, "string", mContext.getPackageName());
        return mContext.getResources().getString(id);
    }

    public boolean isDownloading() {
        return mDownloading;
    }
}

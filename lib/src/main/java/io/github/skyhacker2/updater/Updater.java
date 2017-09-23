package io.github.skyhacker2.updater;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by eleven on 2016/10/25.
 */

public class Updater {
    private static final String TAG = Updater.class.getSimpleName();
    private static final String PREF_DOWNLOAD_ID = "pref_download_id";
    private static final String PREF_ONLINE_VERSION_CODE = "pref_prev_version_code";
    private static final String PREF_JSON = "pref_json";

    public static final String ACTION_ONLINE_PARAMS_UPDATED = "io.github.skyhacker2.updater.ACTION_ONLINE_PARAMS_UPDATED";

    private static Updater mInstance;
    private Activity mContext;
    private String mAppID;
    private int mVersionCode;
    private String mVersionName;
    private String mAppName;
    private boolean mUpdating;
    private long mDownloadId;
    private DownloadManager mDownloadManager;
    private Handler mHandler;
    private String mUpdateUrl;
    private boolean mDebug = false;
    private String mChannelKey = "UMENG_CHANNEL";
    private boolean mInstallFromStore = false;

    BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                mContext.getApplicationContext().unregisterReceiver(mDownloadReceiver);
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == mDownloadId) {
                    Log.d(TAG, "下载完成");
                    mUpdating = false;
                    Cursor c = mDownloadManager.query(new DownloadManager.Query().setFilterById(mDownloadId));
                    if (c != null && c.moveToFirst()) {
                        // 判断是否真的完成
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                            Log.d(TAG, "filename " + filename);
                            Uri uri = Uri.parse("file://" + filename);
                            installApk(uri);
                        }
                    }
                }
            }
        }
    };

    class UpdateThread extends Thread {
        private JSONObject mOnlineAppInfo;
        public UpdateThread() {

        }

        @Override
        public void run() {
            getOnlineVersion();
            if (mOnlineAppInfo != null) {
                if (( mOnlineAppInfo.optInt("versionCode") > mVersionCode) || mDebug ) {
                    Log.d(TAG, "有新版本可以更新");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                            builder.setTitle(getString("app_update_title"));
                            builder.setMessage(mOnlineAppInfo.optString("updateMessage"));
                            builder.setPositiveButton(getString("app_update_ok"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startUpdate();
                                }
                            });
                            builder.setNegativeButton(getString("app_update_later"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mUpdating = false;
                                }
                            });
                            builder.setCancelable(false);
                            builder.create().show();
                        }
                    });
                }
            }
        }

        public void getOnlineVersion() {
            String address = mUpdateUrl;
            try {
                URL url = new URL(address);
                URLConnection connection = url.openConnection();
                InputStream input = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                byte[] combined = new byte[0];
                while ((len=input.read(buffer)) != -1) {
                    byte[] tmp = new byte[combined.length + len];
                    System.arraycopy(combined,0,tmp,0,combined.length);
                    System.arraycopy(buffer,0,tmp,combined.length,len);
                    combined = tmp;
                }
                String json = new String(combined, "utf-8");
                Log.d(TAG, "online version json " + json);
                mOnlineAppInfo = new JSONObject(json);
                Log.d(TAG, "online versionCode " + mOnlineAppInfo.optString("versionCode"));
                Log.d(TAG, "online versionName " + mOnlineAppInfo.optString("versionName"));
                JSONObject onlineParams = mOnlineAppInfo.optJSONObject("onlineParams");
                OnlineParams.setParams(onlineParams);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_ONLINE_PARAMS_UPDATED);
                        mContext.sendBroadcast(intent);
                    }
                });
                SharedPreferences.Editor editor = getSharedPreferencesEditor();
                editor.putString(PREF_JSON, mOnlineAppInfo.toString());
                editor.apply();
//                if (mOnlineAppInfo.getCode() == 0) {
//                    editor.putInt(PREF_ONLINE_VERSION_CODE, mOnlineAppInfo.getVersionCode());
//                    editor.apply();
//                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void startDownload() {
            try {
                ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                String channel = info.metaData.getString(mChannelKey);
                JSONObject channels = mOnlineAppInfo.optJSONObject("channels");
                if (channels != null) {
                    String downloadURL = channels.optString(channel);
                    if (downloadURL == null){
                        downloadURL = channels.optString("source");
                    }
                    if (downloadURL != null){
                        // 注册下载完成广播
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                        mContext.getApplicationContext().registerReceiver(mDownloadReceiver, filter);
                        Toast.makeText(mContext, getString("app_update_start"), Toast.LENGTH_LONG).show();
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadURL));
                        request.setTitle(mAppName);
                        String[] patterns = downloadURL.split("/");
                        String fileName = patterns[patterns.length-1];
                        request = request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        long id = mDownloadManager.enqueue(request);
                        mDownloadId = id;
                        SharedPreferences.Editor editor = getSharedPreferencesEditor();
                        editor.putInt(PREF_ONLINE_VERSION_CODE, mOnlineAppInfo.optInt("versionCode"));
                        editor.putLong(PREF_DOWNLOAD_ID, mDownloadId);

                        editor.apply();
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


        }

        public void startUpdate() {
            if (mInstallFromStore) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName()));
                PackageManager packageManager = mContext.getPackageManager();
                List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean intentSafe = activities.size() > 0;
                if (intentSafe) {
                    mContext.startActivity(intent);
                }
            } else {
                int saveVersionCode = getSharedPreferences().getInt(PREF_ONLINE_VERSION_CODE, -1);
                // 上次有保存
                if (saveVersionCode == mOnlineAppInfo.optInt("versionCode")) {
                    Uri uri = checkIfAlreadyExist();
                    if (uri != null) {
                        Log.d(TAG, "使用已经存在的安装包");
                        installApk(uri);
                    } else {
                        startDownload();
                    }
                } else {
                    startDownload();
                }
            }
        }
    }


    private Updater(){



    }

    private String getString(String key) {
        int id = mContext.getResources().getIdentifier(key, "string", mContext.getPackageName());
        return mContext.getResources().getString(id);
    }

    public static Updater getInstance(Activity context) {
        if (mInstance == null) {
            mInstance = new Updater();
        }
        mInstance.setContext(context);

        return mInstance;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Activity context) {
        mContext = context;
        mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        mHandler = new Handler(mContext.getMainLooper());
        String json = getSharedPreferences().getString(PREF_JSON, null);
        if (json != null) {
            try {
                JSONObject object = new JSONObject(json);
                JSONObject params = object.optJSONObject("onlineParams");
                OnlineParams.setParams(params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAppID() {
        return mAppID;
    }

    public Updater setAppID(String appID) {
        mAppID = appID;
        return mInstance;
    }

    public void checkUpdate() {
        checkUpdate(false);
    }

    public void checkUpdate(boolean goToStore) {
        Log.d(TAG, "开始检查App更新");
        mInstallFromStore = goToStore;

//        mContext.registerReceiver(mDownloadReceiver, filter);

//        if (!isWifi()) {
//            Log.e(TAG, "移动网络");
//            return;
//        }


        PackageManager manager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(mContext.getPackageName(), 0);
            mVersionCode = packageInfo.versionCode;
            mVersionName = packageInfo.versionName;
            mAppName = packageInfo.applicationInfo.name;
            Log.d(TAG, "current versionCode " + mVersionCode);
            Log.d(TAG, "current versionName " + mVersionName);
            int preOnlineVersionCode = getSharedPreferences().getInt(PREF_ONLINE_VERSION_CODE, -1);
            Log.d(TAG, "preOnlineVersionCode " + preOnlineVersionCode);
            if (preOnlineVersionCode == mVersionCode) {
                // 删除已安装的安装包
                Uri uri = checkIfAlreadyExist();
                if (uri != null) {
                    deleteApk(getSharedPreferences().getLong(PREF_DOWNLOAD_ID, -1));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package info not found");
            return;
        }

        synchronized (this) {
            if (!mUpdating) {
                mUpdating = true;
                new UpdateThread().start();
            }
        }

    }

    public String getUpdateUrl() {
        return mUpdateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        mUpdateUrl = updateUrl;
    }

    //// 内部函数
    private boolean isWifi() {
        ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    private SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Updater", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    private SharedPreferences.Editor getSharedPreferencesEditor() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.edit();
    }

    private void installApk(Uri uri) {
        Intent apkIntent = new Intent(Intent.ACTION_VIEW);
        Log.d(TAG, "uri " + uri);
        apkIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(apkIntent);
    }

    private void deleteApk(long id) {
        Log.d(TAG, "删除安装包");
        mDownloadManager.remove(id);
    }

    private Uri checkIfAlreadyExist() {
        long downloadId = getSharedPreferences().getLong(PREF_DOWNLOAD_ID, -1);
        Cursor cursor = mDownloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
        if (cursor != null && cursor.moveToFirst()) {
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                String filename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                Log.d(TAG, "filename " + filename);
                Uri uri = Uri.parse("file://" + filename);
                return uri;
            }
        }
        return null;
    }

    public boolean isDebug() {
        return mDebug;
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }
}

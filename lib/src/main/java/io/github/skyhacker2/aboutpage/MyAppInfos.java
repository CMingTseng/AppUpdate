package io.github.skyhacker2.aboutpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eleven on 2016/12/24.
 */

public class MyAppInfos {
    private final static String TAG = MyAppInfos.class.getSimpleName();
    private static MyAppInfos instance;
    private String mApiURL = "https://raw.githubusercontent.com/skyhacker2/skyhacker2.github.com/master/api/apps";

    private Context mContext;

    private List<AboutItem> mApps = new ArrayList<>();
    private File mAppDir;
    private File mIconsDir;


    private MyAppInfos() {

    }

    public static  MyAppInfos getInstance(Context context) {
        if (instance == null) {
            instance = new MyAppInfos();
        }

        instance.setContext(context);

        return instance;
    }

    /**
     * 初始化文件夹
     */
    private void initFolder() {
        File dir = mContext.getFilesDir();
        File appDir = new File(dir, "apps");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File icons = new File(appDir, "icons");
        if (!icons.exists()) {
            icons.mkdir();
        }

        mAppDir = appDir;
        mIconsDir = icons;
    }

    /**
     * 在线更新我的应用信息
     */
    public void updateAppInfos() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initFolder();
                    URL url = new URL(mApiURL + "/apps.json");
                    URLConnection connection = url.openConnection();
                    String json = getJsonFromConnection(connection);
                    if (json != null) {
                        Log.d(TAG, "json: " + json);
                        File jsonFile = new File(mAppDir, "apps.json");
                        FileOutputStream jsonOut = new FileOutputStream(jsonFile);
                        jsonOut.write(json.getBytes("utf-8"));
                        jsonOut.close();

                        JSONArray array = new JSONArray(json);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject app = array.getJSONObject(i);
                            String iconName = app.optString("icon");
                            if (iconName != null) {
                                File iconFile = new File(mAppDir, iconName);
                                if (!iconFile.exists()) {
                                    URL iconURL = new URL(mApiURL + "/" + app.optString("icon"));
                                    HttpURLConnection iconConnection = (HttpURLConnection) iconURL.openConnection();
                                    if (iconConnection.getResponseCode() == 200) {
                                        saveIconFromConnection(iconConnection, iconName);
                                    }
                                } else {
                                    Log.d(TAG, "icon 已经存在");
                                }
                            }
                        }
                    }

//                    if (mContext instanceof Activity) {
//                        Activity activity = (Activity) mContext;
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                onUpdateCompleted();
//                            }
//                        });
//                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    private void onUpdateCompleted() {
//        File appsFile = new File(mAppDir, "apps.json");
//        if (appsFile.exists()) {
//            mApps.clear();
//            try {
//                FileInputStream inputStream = new FileInputStream(appsFile);
//                byte[] buffer = new byte[inputStream.available()];
//                inputStream.read(buffer);
//                String json = new String(buffer, "utf-8");
//                JSONArray array = new JSONArray(json);
//                for (int i = 0; i < array.length(); i++) {
//                    final JSONObject app = array.getJSONObject(i);
//                    AboutItem item = new AboutItem();
//                    item.title = app.optString("title");
//                    item.subtitle = app.optString("subtitle");
//                    File iconFile = new File(mAppDir, app.optString("icon"));
//                    item.icon = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
//                    item.clickListener = new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + app.optString("packageName")));
//                            mContext.startActivity(intent);
//                        }
//                    };
//                    mApps.add(item);
//                }
//
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private String getJsonFromConnection(URLConnection connection) throws IOException {
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
        input.close();
        String json = new String(combined, "utf-8");

        return json;
    }

    private void saveIconFromConnection(URLConnection connection, String name) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        InputStream input = connection.getInputStream();
        while ((len=input.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        input.close();
        File file = new File(mAppDir, name);
        Bitmap bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.close();
        Log.d(TAG, "save icon: " + file.getAbsoluteFile());
    }

    private void parseJson(String json) {
        try {
            JSONArray array = new JSONArray(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getApiURL() {
        return mApiURL;
    }

    public void setApiURL(String apiURL) {
        if (apiURL.endsWith("/")) {
            apiURL = apiURL.substring(0, apiURL.length()-1);
        }
        mApiURL = apiURL;
    }

    public Context getContext() {
        return mContext;
    }

    private void setContext(Context context) {
        mContext = context;
    }

    public List<AboutItem> getApps() {
        File appsFile = new File(mAppDir, "apps.json");
        if (appsFile.exists()) {
            mApps.clear();
            try {
                FileInputStream inputStream = new FileInputStream(appsFile);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                String json = new String(buffer, "utf-8");
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    final JSONObject app = array.getJSONObject(i);
                    if (app.optString("packageName").equals(mContext.getPackageName())) {
                        continue;
                    }

                    AboutItem item = new AboutItem();
                    item.packageName = app.optString("packageName");
                    item.title = app.optString("title");
                    item.subtitle = app.optString("subtitle");
                    File iconFile = new File(mAppDir, app.optString("icon"));
                    if (!iconFile.exists()) {
                        continue;
                    }
                    item.icon = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
                    item.clickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + app.optString("packageName")));
                            mContext.startActivity(intent);
                        }
                    };
                    mApps.add(item);
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mApps;
    }
}

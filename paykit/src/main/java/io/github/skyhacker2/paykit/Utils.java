package io.github.skyhacker2.paykit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by eleven on 2017/12/15.
 */

public class Utils {

    private final static String TAG = Utils.class.getSimpleName();

    public static String genShortId() {
        Random random = new Random();
        char[] chars = "ABSDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        char[] ids = new char[6];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = chars[random.nextInt(chars.length)];
        }

        return new String(ids);
    }

    public static String genDeviceId() {
        return "device-" + genShortId();
    }

    public static void takeScreenshot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        boolean isCacheEnable = view.isDrawingCacheEnabled();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        // 获取状态栏高度
        Rect frameRect = new Rect();
        view.getWindowVisibleDisplayFrame(frameRect);
        int statusBarHeight = frameRect.top;
        // 获取屏幕长和高
        int width;
        int height;
        Display display = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point point = new Point();
            display.getSize(point);
            width = point.x;
            height = point.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }
        // 去掉标题栏，DecorView是不包含标题栏的
        Bitmap bmpScreenshot = Bitmap.createBitmap(bmp, 0,
                statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(isCacheEnable);

        // 保存到系统相册
        String url = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bmpScreenshot, "LED显示屏激活码", "LED显示屏激活码");
        Log.d(TAG, "保存截图到相册，路径: " + url);
    }

    public static void takeScreenshot(Activity activity, View view) {
        if (view == null) {
            Log.w(TAG, "screenshot:view is null");
        }
        boolean isCacheEnable = view.isDrawingCacheEnabled();
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = view.getDrawingCache();
        try {
            // 保存到系统相册
            String url = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bmp, "LED显示屏激活码", "LED显示屏激活码");
            Log.d(TAG, "保存截图到相册，路径: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(isCacheEnable);
        }
    }

    public static void takeScreenshot(Activity activity, View view, File saveFile) {
        if (view == null) {
            Log.w(TAG, "screenshot:view is null");
        }
        boolean isCacheEnable = view.isDrawingCacheEnabled();
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = view.getDrawingCache();
        try {
            FileOutputStream outputStream = new FileOutputStream(saveFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            // 通知系统扫描图片文件
            Uri uri = Uri.fromFile(saveFile);
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            activity.sendBroadcast(mediaScanIntent);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(isCacheEnable);
        }
    }
}

package io.github.skyhacker2.rater;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

/**
 * Created by eleven on 2016/12/24.
 */

public class Rater {

    public static void rateApp(Activity context, int titleRes, int messageRes) {
        rateApp(context, context.getString(titleRes), context.getString(messageRes));
    }

    public static void rateApp(Activity context, String title, String message) {
        SharedPreferences preferences = context.getSharedPreferences("AppRate", Activity.MODE_PRIVATE);
        boolean hasRate = preferences.getBoolean("has_rate", false);
        long lanuchCount = preferences.getLong("lanuch_count", 0);
        if (!hasRate && lanuchCount == 1) {
            Rater.showRateDialog(context, title, message);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lanuch_count", lanuchCount + 1);
        editor.apply();

    }

    public static void showRateDialog(final Activity context, String title, String message) {
        final SharedPreferences preferences = context.getSharedPreferences("AppRate", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("残忍拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("has_rate", true);
                        editor.apply();
                    }
                }).create().show();
    }
}

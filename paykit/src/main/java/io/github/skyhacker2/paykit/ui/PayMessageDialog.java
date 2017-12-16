package io.github.skyhacker2.paykit.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import io.github.skyhacker2.paykit.PayKit;
import io.github.skyhacker2.paykit.Utils;

/**
 * Created by eleven on 2017/12/9.
 */

public class PayMessageDialog extends AlertDialog {
    public interface OnActivateClickListener {
        void onActivateClicked();
    }

    public PayMessageDialog(@NonNull Context context) {
        super(context);
    }

    public PayMessageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public PayMessageDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static void show(final Context context, final String title, final String message, final double price,
                            final PayKit.PayCallback payCallback,
                            final OnActivateClickListener onActivateClickListener) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("支付", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PayKit.getInstance().pay(context, title, message, price, payCallback);
                    }
                })
                .setNegativeButton("取消", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNeutralButton("激活", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onActivateClickListener != null) {
                            onActivateClickListener.onActivateClicked();
                        }
                    }
                }).create().show();
    }
}

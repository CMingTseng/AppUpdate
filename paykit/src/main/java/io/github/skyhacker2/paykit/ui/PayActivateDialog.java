package io.github.skyhacker2.paykit.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import io.github.skyhacker2.paykit.PayKit;
import io.github.skyhacker2.paykit.R;

/**
 * Created by eleven on 2017/12/16.
 */

public class PayActivateDialog extends AlertDialog {
    public PayActivateDialog(@NonNull Context context) {
        super(context);
    }

    public PayActivateDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public PayActivateDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static void show(final Context context, final PayKit.ActivateCallback callback) {
        final Handler handler = new Handler(context.getMainLooper());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_pay_activate, null);
        final TextInputLayout textInputLayout = (TextInputLayout)view.findViewById(R.id.input_layout);
        final TextInputEditText textInputEditText = (TextInputEditText)view.findViewById(R.id.input);
        builder.setTitle("激活操作")
                .setView(view)
                .setPositiveButton("激活", null)
                .setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = ((AlertDialog)dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PayKit.getInstance().activateWithId(context, textInputEditText.getText().toString(), new PayKit.ActivateCallback() {
                            @Override
                            public void onActivated(String activationId) {
                                if (callback != null) {
                                    callback.onActivated(activationId);
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialogInterface.dismiss();
                                    }
                                });
                            }

                            @Override
                            public void onFailed(String reason) {
                                textInputLayout.setError(reason);
                            }
                        });
                    }
                });

                Button cancelBtn = ((AlertDialog)dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogInterface.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }
}

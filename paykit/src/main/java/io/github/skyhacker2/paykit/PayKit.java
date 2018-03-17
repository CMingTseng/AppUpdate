package io.github.skyhacker2.paykit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

import c.b.BP;
import c.b.PListener;
import c.b.QListener;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import io.github.skyhacker2.paykit.models.ActivationModel;

/**
 * Created by eleven on 2017/12/9.
 */

public class PayKit {
    private static final String TAG = PayKit.class.getSimpleName();
    public interface PayCallback {
        void onPayed();
        void onFailed(String reason);
    }

    public interface ActivateCallback {
        void onActivated(String activationId);
        void onFailed(String reason);
    }

    public interface InitCallback {
        void onInitFinished();
        void onActivateInOtherDevice();
    }

    private static final String PREF_NAME = "paykit";
    private static final String KEY_ORDER_ID = "orderId";
    private static final String KEY_ACTIVATION = "activation";
    private static final String KEY_DEVICE_ID = "deviceId";
    private static final String KEY_ACTIVATION_ID = "activationId";
    private static PayKit sInstance;
    private String mOrderId;        // 订单号
    private boolean mActivation;    // 激活状态
    private String mDeviceId;       // 设备id，随机生成
    private String mActivationId;   // 本机存储的激活码

    public static PayKit getInstance() {
        if (sInstance == null) {
            sInstance = new PayKit();
        }
        return sInstance;
    }

    /**
     * 初始化方法
     * @param context
     * @param appId
     */
    public void init(Context context, String appId, InitCallback initCallback) {
        BP.init(appId);
        Bmob.initialize(context, appId);
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mOrderId = preferences.getString(KEY_ORDER_ID, null);
        mActivation = preferences.getBoolean(KEY_ACTIVATION, false);
        mDeviceId = preferences.getString(KEY_DEVICE_ID, null);
        mActivationId = preferences.getString(KEY_ACTIVATION_ID, null);

        Log.d(TAG, "orderId: " + mOrderId);
        Log.d(TAG, "activation: " + mActivation);
        Log.d(TAG, "deviceId: " + mDeviceId);

        if (mDeviceId == null) {
            mActivation = false;
            if (initCallback != null){
                initCallback.onInitFinished();
            }
        } else {
            checkDeviceId(context, mDeviceId, initCallback);
        }
    }

    /**
     * 支付
     * @param context
     * @param title
     * @param desc
     * @param price
     * @param callback
     */
    public synchronized void pay(final Context context, String title, String desc, double price, final PayCallback callback) {
        BP.pay(title, desc, price, BP.PayType_Alipay, new PListener() {
            @Override
            public void orderId(String s) {
                mOrderId = s;
                saveOrderId(context, s);
            }

            @Override
            public void succeed() {
                if (callback != null) {
                    callback.onPayed();
                }
            }

            @Override
            public void fail(int i, String s) {
                Log.e(TAG, "faid " + i + " reason" + s);
                if (callback != null) {
                    callback.onFailed(s);
                }
            }
        });
    }

    /**
     * 激活App
     * @param context
     * @param callback
     */
    public synchronized void activate(final Context context, final ActivateCallback callback) {
        // 之前已经激活，直接返回成功
        if (mActivation) {
            if (callback != null) {
                callback.onActivated(mActivationId);
            }
        } else {
            // 检查订单是否付款
            if (mOrderId != null) {
                Log.d(TAG, "检查订单号 " + mOrderId);
                BP.query(mOrderId, new QListener() {
                    @Override
                    public void succeed(String s) {
                        Log.d(TAG, "status " + s);
                        if (callback != null) {
                            if ("NOTPAY".equals(s)) {
                                callback.onFailed("订单未付款");
                            } else {
                                mActivation = true;
                                saveActivation(context, mActivation);
                                mDeviceId = Utils.genDeviceId();
                                saveDeviceId(context, mDeviceId);

                                // 生成序列号并保存到服务器
                                final ActivationModel activationModel = new ActivationModel();
                                activationModel.setActivationId(Utils.genShortId());
                                activationModel.setOrderId(mOrderId);
                                activationModel.setDeviceId(mDeviceId);
                                activationModel.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String objectId, BmobException e) {
                                        if (e == null) {
                                            mActivationId = activationModel.getActivationId();
                                            saveActivationId(context, mActivationId);
                                            callback.onActivated(mActivationId);
                                        } else {
                                            callback.onFailed(e.getMessage());
                                        }
                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void fail(int i, String s) {
                        if (callback != null) {
                            callback.onFailed("无效订单号");
                        }
                    }
                });
            } else {
                callback.onFailed("无订单号");
            }
        }
    }


    public synchronized void activateWithId(final Context context, String activationId, final ActivateCallback callback) {
        BmobQuery<ActivationModel> query = new BmobQuery<>();
        Log.d(TAG, "activateWithId " + activationId);
        query.addWhereEqualTo("activationId", activationId);
        query.findObjects(new FindListener<ActivationModel>() {
            @Override
            public void done(List<ActivationModel> list, BmobException e) {
                if (e == null) {
                    Log.d(TAG, "done " + list.size());
                    if (list.size() == 0) {
                        if (callback != null) {
                            callback.onFailed("无效激活码");
                        }
                    } else {
                        ActivationModel model = list.get(0);
                        mDeviceId = Utils.genDeviceId();
                        saveDeviceId(context, mDeviceId);
                        mOrderId = model.getOrderId();
                        Log.d(TAG, "orderId " + mOrderId);
                        saveOrderId(context, mOrderId);
                        mActivationId = list.get(0).getActivationId();
                        saveActivationId(context, mActivationId);

                        // 更换了手机激活
                        model.setDeviceId(mDeviceId);
                        model.update();

                        // 检查订单号是否已付款
                        BP.query(mOrderId, new QListener() {
                            @Override
                            public void succeed(String s) {
                                if ("NOTPAY".equals(s)) {
                                    callback.onFailed("订单未付款");
                                } else {
                                    mActivation = true;
                                    saveActivation(context, mActivation);
                                    callback.onActivated(mActivationId);
                                }
                            }

                            @Override
                            public void fail(int i, String s) {
                                callback.onFailed("无效订单号");
                            }
                        });
                    }

                } else {
                    if (callback != null) {
                        callback.onFailed(e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * 检查deviceId是否同一个
     * @param deviceId
     */
    private synchronized void checkDeviceId(final Context context, final String deviceId, final InitCallback initCallback) {
        BmobQuery<ActivationModel> query = new BmobQuery<>();
        query.addWhereEqualTo("orderId", mOrderId);
        query.findObjects(new FindListener<ActivationModel>() {
            @Override
            public void done(List<ActivationModel> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        ActivationModel model = list.get(0);
                        // 有另外一台设备使用了激活码
                        if (!model.getDeviceId().equals(deviceId)) {
                            mActivation = false;
                            saveActivation(context, mActivation);
                            if (initCallback != null) {
                                initCallback.onActivateInOtherDevice();
                            }
                        } else {
                            if (initCallback != null) {
                                initCallback.onInitFinished();
                            }
                        }
                    } else {
                        mActivation = false;
                        saveActivation(context, mActivation);
                        if (initCallback != null) {
                            initCallback.onInitFinished();
                        }
                    }
                } else {
                    if (initCallback != null) {
                        initCallback.onInitFinished();
                    }
                }
            }
        });
    }

    private void saveOrderId(Context context, String orderId) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ORDER_ID, orderId);
        editor.apply();
    }

    private void saveActivation(Context context, boolean activation) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_ACTIVATION, activation);
        editor.apply();
    }

    private void saveDeviceId(Context context, String deviceId) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.apply();
    }

    private void saveActivationId(Context context, String activationId) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_ACTIVATION_ID, activationId);
        editor.apply();
    }

    public String getOrderId() {
        return mOrderId;
    }

    public boolean isActivation() {
        return mActivation;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getActivationId() {
        return mActivationId;
    }

    public static boolean isActiveation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_ACTIVATION, false);
    }
}

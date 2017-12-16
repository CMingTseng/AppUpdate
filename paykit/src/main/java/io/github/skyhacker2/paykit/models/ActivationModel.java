package io.github.skyhacker2.paykit.models;

import cn.bmob.v3.BmobObject;

/**
 * Created by eleven on 2017/12/16.
 */

public class ActivationModel extends BmobObject {
    /**
     * 激活码，6位
     */
    private String activationId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 当前设备的，用来判断是否换了设备，需要重新激活app
     */
    private String deviceId;

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

package io.github.skyhacker2.updater;

import org.json.JSONObject;

/**
 * Created by eleven on 2016/10/25.
 */

public class OnlineParams {
    private static JSONObject mParams;
    public static String get(String key, String def) {
        if (mParams != null) {
            return mParams.optString(key, def);
        } else {
            return def;
        }
    }

    public static JSONObject getParams() {
        return mParams;
    }

    public static void setParams(JSONObject params) {
        mParams = params;
    }
}

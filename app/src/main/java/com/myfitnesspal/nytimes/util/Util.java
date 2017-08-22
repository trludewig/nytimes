package com.myfitnesspal.nytimes.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by tludewig on 8/20/17.
 * Class to store any utility methods.
 */

public class Util {

    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo == null || !activeInfo.isConnected()
                ||  ((activeInfo.getType() != ConnectivityManager.TYPE_WIFI
                && activeInfo.getType() != ConnectivityManager.TYPE_MOBILE))) {
            return false;
        }
        return true;
    }
}

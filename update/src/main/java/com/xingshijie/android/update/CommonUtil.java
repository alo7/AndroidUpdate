package com.xingshijie.android.update;

import android.content.Context;

/**
 *
 * Created by word on 16/8/31.
 */

public class CommonUtil {

    public static int getVersionCode(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception ex) {
            return 0;
        }
    }
}

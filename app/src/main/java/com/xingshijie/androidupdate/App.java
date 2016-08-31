package com.xingshijie.androidupdate;

import android.app.Application;

import com.xingshijie.android.update.UpdateUtil;


/**
 *
 * Created by word on 16/8/31.
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateUtil.setRelUrl("http://xingshijie.github.io/onlineConfig");
        UpdateUtil.checkUpdate(this);
    }
}

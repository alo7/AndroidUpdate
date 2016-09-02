package com.xingshijie.androidupdate;

import android.app.Application;

import com.xingshijie.android.update.UpdateAgent;


/**
 *
 * Created by word on 16/8/31.
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateAgent.setRelUrl("http://xingshijie.github.io/onlineConfig");
        UpdateAgent.checkUpdate(this);
    }
}

package com.alo7.androidupdate;

import android.app.Application;

import com.alo7.android.update.UpdateAgent;


/**
 *
 * Created by word on 16/8/31.
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //设置配置文件的Url
        UpdateAgent.setConfigUrl("http://xingshijie.github.io/onlineConfig/config.json");
    }
}

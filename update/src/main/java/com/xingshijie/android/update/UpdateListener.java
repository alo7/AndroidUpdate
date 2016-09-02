package com.xingshijie.android.update;

/**
 * Created by shijie.xing on 16/9/2.
 */
public interface UpdateListener {

    int HAS_UPDATE = 0;
    int NO_UPDATE = 1;
    int FORCE_UPDATE = 2;
    int TIME_OUT = 3;

    void onUpdateReturned(int updateStatus, OnlineConfig config);
}

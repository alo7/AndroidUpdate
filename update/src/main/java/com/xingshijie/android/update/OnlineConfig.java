package com.xingshijie.android.update;


import org.json.JSONObject;

import java.io.Serializable;

public class OnlineConfig implements Serializable{

    /**
     * lastVersionCode : 9000
     * downloadUrl : url
     * releaseNotes : 应用更新说明，需要弹框
     * minimumRequiredVersion : 9000
     * isUpdateOnlyWifi : true
     * patches : []
     * apkSize : 20.6
     * isDeltaUpdate : false
     * isSilentDownload : false
     */

    private int lastVersionCode;
    private String downloadUrl;
    private String releaseNotes;
    private int minimumRequiredVersion;
    private Boolean isUpdateOnlyWifi;
    private double apkSize;

    public int getLastVersionCode() {
        return lastVersionCode;
    }

    public void setLastVersionCode(int lastVersionCode) {
        this.lastVersionCode = lastVersionCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public int getMinimumRequiredVersion() {
        return minimumRequiredVersion;
    }

    public void setMinimumRequiredVersion(int minimumRequiredVersion) {
        this.minimumRequiredVersion = minimumRequiredVersion;
    }

    public Boolean isIsUpdateOnlyWifi() {
        return isUpdateOnlyWifi;
    }

    public void setIsUpdateOnlyWifi(Boolean isUpdateOnlyWifi) {
        this.isUpdateOnlyWifi = isUpdateOnlyWifi;
    }

    public double getApkSize() {
        return apkSize;
    }

    public void setApkSize(double apkSize) {
        this.apkSize = apkSize;
    }

}

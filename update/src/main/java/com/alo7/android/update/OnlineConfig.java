package com.alo7.android.update;


import org.json.JSONObject;

import java.io.Serializable;

public class OnlineConfig implements Serializable{

    /**
     * lastVersionCode : 9000
     * downloadUrl : url
     * releaseNotes : 应用更新说明，需要弹框
     * minimumRequiredVersion : 9000
     * isUpdateOnlyWifi : true
     */

    private int lastVersionCode;
    private String downloadUrl;
    private String releaseNotes;
    private int minimumRequiredVersion;
    private Boolean isUpdateOnlyWifi;
    private double apkSize;

    public OnlineConfig(){

    }

    public OnlineConfig(JSONObject jsonObject){
        //自带的jsonObject加载不存在的key值会出异常,所以只有这样了,因为没法保证一定存在这个字段
        try {
            lastVersionCode = jsonObject.getInt("lastVersionCode");
        } catch (Exception e) {
        }

        try {
            downloadUrl = jsonObject.getString("downloadUrl");
        } catch (Exception e) {

        }

        try {
            releaseNotes = jsonObject.getString("releaseNotes");
        } catch (Exception e) {

        }

        try {
            minimumRequiredVersion = jsonObject.getInt("minimumRequiredVersion");
        } catch (Exception e) {

        }

        try {
            isUpdateOnlyWifi = jsonObject.getBoolean("isUpdateOnlyWifi");
        } catch (Exception e) {

        }

        try {
            apkSize = jsonObject.getDouble("apkSize");
        } catch (Exception e) {

        }

    }

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

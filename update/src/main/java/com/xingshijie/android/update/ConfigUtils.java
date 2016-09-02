package com.xingshijie.android.update;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConfigUtils {

    private static String REL_URL = "http://static-data.alo7.com/axt/android";
    private static String CONFIG_FILE_NAME = "config.json";
    private static OnlineConfig combinedConfig;
    private static String configUrl;


    private static void updateOnlineConfig(final Context context, final ConfigListener configListener) {
        final Context appContext = context.getApplicationContext();

        if (context.checkPermission(Manifest.permission.INTERNET,
                android.os.Process.myPid(), Process.myUid()) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Please add Permission in AndroidManifest!", Toast.LENGTH_SHORT).show();
            if (configListener != null) {
                configListener.config(null);
            }
            return;
        }

        new AsyncTask<Void, Void, OnlineConfig>() {

            @Override
            protected OnlineConfig doInBackground(Void... params) {
                OnlineConfig globalConfig = getJSON(getGlobalConfigUrl());
                OnlineConfig versionConfig = getJSON(getSpecifiedVersionConfigUrl(appContext));
                return mergeConfig(globalConfig, versionConfig);
            }

            @Override
            protected void onPostExecute(OnlineConfig onlineConfig) {
                if (configListener != null) {
                    combinedConfig = onlineConfig;
                    configListener.config(onlineConfig);
                }
            }
        }.execute();
    }

    /**
     * 设置配置文件的相对路径,此路径下必须存在配置文件
     * rel_url + "/config.json"
     * @param relUrl  relUrl cant null or end with '/'
     */
    static void setRelUrl(String relUrl) {
        if (TextUtils.isEmpty(relUrl)) {
            throw new IllegalArgumentException("relUrl can't null");
        } else if (relUrl.endsWith("/")) {
            throw new IllegalArgumentException("relUrl can't end with '/'");
        } else {
            REL_URL = relUrl;
        }
    }

    /**
     * 可以直接使用一个配置表完成更新
     *
     * @param url 配置表url的绝对路径
     */
    static void setConfigUrl(String url) {
        configUrl = url;
    }


    public static void updateOnlineConfig(Context context) {
        updateOnlineConfig(context, null);
    }

    private static String getGlobalConfigUrl() {
        if (configUrl != null) {
            return configUrl;
        } else {
            return REL_URL + "/" + CONFIG_FILE_NAME;
        }
    }

    private static String getSpecifiedVersionConfigUrl(Context context) {
        if (configUrl != null) {
            return null;
        } else {
            return REL_URL + "/" + CommonUtil.getVersionCode(context) + "/" + CONFIG_FILE_NAME;
        }
    }

    public static void getConfig(Context context, ConfigListener configListener) {
        if (combinedConfig != null) {
            configListener.config(combinedConfig);
        } else {
            updateOnlineConfig(context, configListener);
        }
    }

    private static OnlineConfig mergeConfig(OnlineConfig globalConfig, OnlineConfig versionConfig) {
        OnlineConfig mergedConfig = new OnlineConfig();
        if (globalConfig == null) {
            mergedConfig = versionConfig;
        } else if (versionConfig == null) {
            mergedConfig = globalConfig;
        } else {
            mergedConfig.setLastVersionCode(versionConfig.getLastVersionCode() > 0 ?
                    versionConfig.getLastVersionCode() : globalConfig.getLastVersionCode());
            mergedConfig.setDownloadUrl(versionConfig.getDownloadUrl() != null ?
                    versionConfig.getDownloadUrl() : globalConfig.getDownloadUrl());
            mergedConfig.setReleaseNotes(versionConfig.getReleaseNotes() != null ?
                    versionConfig.getReleaseNotes() : globalConfig.getReleaseNotes());
            mergedConfig.setMinimumRequiredVersion(versionConfig.getMinimumRequiredVersion() > 0 ?
                    versionConfig.getMinimumRequiredVersion() : globalConfig.getMinimumRequiredVersion());
            mergedConfig.setApkSize(versionConfig.getApkSize() > 0 ?
                    versionConfig.getApkSize() : globalConfig.getApkSize());
            mergedConfig.setIsUpdateOnlyWifi(versionConfig.isIsUpdateOnlyWifi() != null ?
                    versionConfig.isIsUpdateOnlyWifi() : globalConfig.isIsUpdateOnlyWifi());
        }

        return mergedConfig;
    }


    private static OnlineConfig getJSON(String url) {
        if (url == null) {
            return null;
        }

        int timeout = 3000;
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    return new OnlineConfig(new JSONObject(sb.toString()));
            }

        } catch (IOException ex) {
            Logger.getLogger("configUtils").log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger("configUtils").log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger("configUtils").log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }

    interface ConfigListener {
        void config(OnlineConfig config);
    }

}
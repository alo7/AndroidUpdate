package com.alo7.android.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;
import android.widget.Toast;


public class ConfigUtils {

    private static String relUrl;
    private static String configFileName = "config.json";
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
            ConfigUtils.relUrl = relUrl;
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

    /**
     * 默认配置文件名为config.json,可以自定义配置文件名,比如说你有release1,和release2两个productFlavor
     * ,你就可以设置两个版本使用不同的配置文件名,release1.json,release2.json,这样你就可以直接
     * 在同一个目录下放置不同productFlavor的配置文件
     *
     * @param fileName 配置文件的名字
     */
    public static void setConfigFileName(String fileName) {
        configFileName = fileName;
    }


    public static void updateOnlineConfig(Context context) {
        updateOnlineConfig(context, null);
    }

    private static String getGlobalConfigUrl() {
        checkUrl();
        if (configUrl != null) {
            return configUrl;
        } else {
            return relUrl + "/" + configFileName;
        }
    }

    private static String getSpecifiedVersionConfigUrl(Context context) {
        checkUrl();
        if (configUrl != null) {
            return null;
        } else {
            return relUrl + "/" + CommonUtil.getVersionCode(context) + "/" + configFileName;
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

    private static void checkUrl() {
        if(TextUtils.isEmpty(relUrl) && TextUtils.isEmpty(configUrl)) {
            throw new IllegalArgumentException(
                    "Android Update must set relative url or config url");
        }
    }

    interface ConfigListener {
        void config(OnlineConfig config);
    }

}
package com.alo7.android.update;

import java.io.File;
import java.util.UUID;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;


public class UpdateAgent {

    public static final String SHARED_PREFERENCES_NAME = "update";
    private static final String IGNORE_VERSION = "saveIgnoreVersion";
    private static final String DOWNLOADED_APK_PATH = "downloaded_apk_path";
    private static final String DOWNLOADED_VERSION_CODE = "downloaded_version_code";

    /**
     * 用于多表配置,设置配置文件的相对路径,此路径下必须存在配置文件
     * rel_url + "/config.json"
     * 默认的配置文件名为config.json, 可使用 {@link ConfigUtils#setConfigFileName(String)} 设置文件名
     *
     * @param relUrl relUrl cant null or end with '/'
     */
    public static void setRelUrl(String relUrl) {
        ConfigUtils.setRelUrl(relUrl);
    }

    /**
     * 可以直接使用一个配置表完成更新
     *
     * @param url 配置表url的绝对路径
     */
    public static void setConfigUrl(String url) {
        ConfigUtils.setConfigUrl(url);
    }

    /**
     * 检查更新,用于应用启动时调用,如果用户已经点击忽略此版本,则此版本的更新提示不再显示.
     *
     * @param context context
     */
    public static void checkUpdate(final Context context) {
        ConfigUtils.getConfig(context, new ConfigUtils.ConfigListener() {
            @Override
            public void config(OnlineConfig config) {
                if (config == null) {
                    return;
                }
                if (CommonUtil.getVersionCode(context) < config.getMinimumRequiredVersion()) {
                    showForceUpdate(context, config);
                } else if (CommonUtil.getVersionCode(context) < config.getLastVersionCode()) {
                    if (!isIgnoreVersion(context, config.getLastVersionCode())) {
                        showUpdateDialog(context, config);
                    }
                }
            }
        });
    }

    public static void checkUpdateWithCustomerListener(final Context context, final UpdateListener listener) {
        ConfigUtils.getConfig(context, new ConfigUtils.ConfigListener() {
            @Override
            public void config(OnlineConfig config) {
                if (listener == null) {
                    return;
                }
                if (config == null) {
                    listener.onUpdateReturned(UpdateListener.TIME_OUT, config);
                    return;
                }
                if (CommonUtil.getVersionCode(context) < config.getMinimumRequiredVersion()) {
                    listener.onUpdateReturned(UpdateListener.FORCE_UPDATE, config);
                } else if (CommonUtil.getVersionCode(context) < config.getLastVersionCode()) {
                    listener.onUpdateReturned(UpdateListener.HAS_UPDATE, config);
                } else {
                    listener.onUpdateReturned(UpdateListener.NO_UPDATE, config);
                }
            }
        });
    }


    /**
     * 强制检查更新,如果有更新的话,肯定会弹出更新Dialog,不受是否忽略版本或者已经显示更新Dialog的影响,
     * 适用于用户主动点击检查更新
     *
     * @param context context
     */
    public static void forceCheckUpdate(final Context context) {
        ConfigUtils.getConfig(context, new ConfigUtils.ConfigListener() {
            @Override
            public void config(OnlineConfig config) {
                if (config == null) {
                    return;
                }
                if (CommonUtil.getVersionCode(context) < config.getLastVersionCode()) {
                    showUpdateDialog(context, config);
                } else {
                    Toast.makeText(context.getApplicationContext(), "已经是最新版本", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void showForceUpdate(final Context context, OnlineConfig config) {
        Intent intent = UpdateDialogActivity.getIntent(context, config, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void showUpdateDialog(Context context, final OnlineConfig config) {
        Intent intent = UpdateDialogActivity.getIntent(context, config, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void saveIgnoreVersion(Context context, int versionCode) {
        SharedPreferences.Editor editor = context.getApplicationContext().
                getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(IGNORE_VERSION, versionCode);
        editor.commit();
    }

    public static boolean isIgnoreVersion(Context context, int versionCode) {
        int ignoreVersion = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE).getInt(IGNORE_VERSION, 0);
        return ignoreVersion == versionCode;
    }

    private static void saveDownloadedApkPathAndVersionCode(Context context, String path, int versionCode) {
        SharedPreferences.Editor editor = context.getApplicationContext().
                getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(DOWNLOADED_APK_PATH, path);
        editor.putInt(DOWNLOADED_VERSION_CODE, versionCode);
        editor.commit();
    }

    /**
     * @param versionCode 使用versionCode来判别Apk文件是否下载,以后可以改成根据md5判断文件是否下载
     * @return 返回下载文件的路径, 如果不存在, 返回null
     */
    private static File getDownloadApkFile(Context context, int versionCode) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            int downloadedVersionCode = sp.getInt(DOWNLOADED_VERSION_CODE, 0);
            if (versionCode == downloadedVersionCode) {
                String path = sp.getString(DOWNLOADED_APK_PATH, "");
                File file = new File(path);
                if (file.isFile()) {
                    return file;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void downloadApkAndInstall(Context context, final OnlineConfig config) {
        try {
            String url = config.getDownloadUrl();
            int versionCode = config.getLastVersionCode();
            File file = getDownloadApkFile(context, config.getLastVersionCode());
            if (file != null) {
                promptInstall(context, Uri.fromFile(file));
                return;
            }

            final Context applicationContext = context.getApplicationContext();
            final DownloadManager downloadManager = (DownloadManager) applicationContext
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            // java.lang.IllegalArgumentException: Can only download HTTP/HTTPS URIs: url
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.setDestinationInExternalPublicDir(
                        Environment.getExternalStorageDirectory().getAbsolutePath(),
                        UUID.randomUUID() + ".apk");
            }
            final long enqueueId = downloadManager.enqueue(request);
            final BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long downloadCompletedId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    // 检查是否是自己的下载队列 id, 有可能是其他应用的
                    if (enqueueId != downloadCompletedId) {
                        return;
                    }
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueueId);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        // 下载失败也会返回这个广播，所以要判断下是否真的下载成功
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            // 获取下载好的 apk 路径
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                            saveDownloadedApkPathAndVersionCode(applicationContext, uriString, config.getLastVersionCode());
                            // 提示用户安装
                            promptInstall(applicationContext, Uri.parse("file://" + uriString));
                        }
                    }
                    applicationContext.unregisterReceiver(this);
                }
            };
            applicationContext.registerReceiver(receiver, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void promptInstall(Context context, Uri data) {
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, "application/vnd.android.package-archive");
        // FLAG_ACTIVITY_NEW_TASK 可以保证安装成功时可以正常打开 app
        promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(promptInstall);
    }

}

package com.xingshijie.android.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;




public class UpdateUtil {

    private static final String SHARED_PREFERENCES_NAME = "update";
    private static final String IGNORE_VERSION = "saveIgnoreVersion";

    private static boolean hasShownUpdateDialog = false;
    private static boolean hasShownForceUpdateDialog = false;

    /**
     * 设置配置文件的相对路径,此路径下必须存在配置文件
     * rel_url + "/config.json"
     * @param relUrl  relUrl cant null or end with '/'
     */
    public static void setRelUrl(String relUrl) {
        ConfigUtils.setRelUrl(relUrl);
    }

    public static void checkUpdate(final Context context) {
        ConfigUtils.getConfig(context, new ConfigUtils.ConfigListener() {
            @Override
            public void config(OnlineConfig config) {
                if (config == null) {
                    return;
                }
                if (CommonUtil.getVersionCode(context) < config.getMinimumRequiredVersion()) {
                    showForceUpdate(context);
                } else if (CommonUtil.getVersionCode(context) < config.getLastVersionCode()) {
                    if (!hasShownUpdateDialog && !isIgnoreVersion(context, config.getLastVersionCode())) {
                        showUpdateDialog(context, config);
                        hasShownUpdateDialog = true;
                    }
                }
            }
        });
    }

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

    public static void showForceUpdate(final Context context) {
        if (hasShownForceUpdateDialog) {
            return;
        }
        ConfigUtils.getConfig(context, new ConfigUtils.ConfigListener() {
            @Override
            public void config(OnlineConfig config) {
                if (config == null) {
                    return;
                }
                Intent intent = UpdateDialogActivity.getIntent(context, config, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        hasShownForceUpdateDialog = true;
    }

    private static void showUpdateDialog(Context context, final OnlineConfig config) {
        Intent intent = UpdateDialogActivity.getIntent(context, config, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void saveIgnoreVersion(Context context, int versionCode){
        SharedPreferences.Editor editor = context.getApplicationContext().
                getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(IGNORE_VERSION, versionCode);
        editor.commit();
    }

    public static boolean isIgnoreVersion(Context context, int versionCode){
        int ignoreVersion = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE).getInt(IGNORE_VERSION, 0);
        return ignoreVersion == versionCode;
    }

    public static void downloadApkAndInstall(Context context, String url) {
        try {
            final Context applicationContext = context.getApplicationContext();
            final DownloadManager downloadManager = (DownloadManager) applicationContext
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            // java.lang.IllegalArgumentException: Can only download HTTP/HTTPS URIs: url
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

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

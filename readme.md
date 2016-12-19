# Android Update

Android Update 是一个基于配置文件的自动更新方案，可以通过一张在线配置表实现应用自动更新，强制更新。项目中使用了很多Umeng的资源文件，使用方法也和Umeng Update很相似，可以无缝替换友盟更新，在此感谢Umeng为我们开发者提供了那么多方便的服务。

## 使用方法

Gradle

```groovy
repositories {
   jcenter()
}
compile 'com.alo7.android.update:Update:0.5'
```

调用所有方法之前，必须先调用UpdateAgent.setConfigUrl()设置配置文件的url。

```java
public class MainActivity extends Activity {
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      	//设置配置文件的url
      	UpdateAgent.setConfigUrl("http://xingshijie.github.io/onlineConfig/config.json");
		UpdateAgent.checkUpdate(MainActivity.this);
    }
}
```

## 配置文件的建立 

```json
{
  "lastVersionCode" : 251,  //最新版本的版本号
  "minimumRequiredVersion": 250, //如果当前版本号小于此版本，引导强制更新
  "downloadUrl": "http://xingshijie.github.io/myapk/app-update-debug.apk",
  "releaseNotes": "应用更新说明，需要弹框"
}
```

解析配置表的逻辑

```java
if (CommonUtil.getVersionCode(context) < config.getMinimumRequiredVersion()) {
     showForceUpdate(context, config);
} else if (CommonUtil.getVersionCode(context) < config.getLastVersionCode()) {
     if (!isIgnoreVersion(context, config.getLastVersionCode())) {
          showUpdateDialog(context, config);
     }
}
```

首先通过比较应用的版本号和配置表的minimumRequiredVersion最低要求版本号判断是否需要强制更新，如果不需要强制更新的话就再通过比较lastVersionCode判断是否是最新版本，如果不是最新版本且用户之前没有点过忽略版本的话就弹出更新。

### 使用GitHub的个人博客保存文件

http://xingshijie.github.io/onlineConfig/config.json

## 多表配置

可以通过设置UpdateAgent.setRelUrl()来支持多表配置，多表配置可以只向指定版本下发配置文件，可以通过此方法实现特定版本的更新或者其它诸如补丁下发的之类的功能，详情在这里[multiConfig.md](multiConfig.md)

## 关于多渠道更新 （待实现）

只需要在配置表里加入一个字段，比如说

```json
{
  "channel" : [
   	"QQ":"downloadUrl"	//如果需要多个字段的话可以在建一个对象，这里我们只需要下载地址不同就可以了
  ]
}
```

```java
if (config.getChannel() != null) {
  String channelDownloadUrl = config.getChannel.get(BuildConfig.channel);
  if(TextUtils.isEmpty(channelDownloadUrl)){
    config.setDownloadUrl(channelDownloadUrl);
  } 
}
```



## 关于增量更新

大概原理就就是服务器根据新旧apk差别生成增量更新包，客户端根据增量更新包在本地合成新的Apk安装包




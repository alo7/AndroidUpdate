# 如何仅用文件服务器实现自动更新和补丁下发

友盟更新替代方案，纯客户端实现自动更新以及补丁下发

原理很简单，就是客户端直接向任意文件服务器请求配置文件，然后根据配置文件执行操作。

但关键是配置文件该怎么建立，客户端该如何解析配置文件

## 配置文件的建立,如下图所示

![](image/image1.png)

上图有几个关键点

+ 主目录下及onlineConfig/config.json 代表全局配置，如果应用希望向所有版本配置参数就可以修改此文件

+ 250/config.json或251/config.json代表指定版本的配置文件，特定版本的配置文件只有指定版本会收到

+ 使用版本号作为版本唯一标志符，版本号唯一，版本配置唯一，当然也可以使用versionName或者两者联合使用

+ 特定版本的配置文件与全局配置文件结构保持一致

+ 客户端将使用全局配置文件和当前版本的配置文件合成一个最终的配置文件,合成规则：

  + 如果没有当前版本配置文件的话直接采用全局配置文件
  + 当前版本的配置文件里的字段覆盖全局配置文件里的字段，注意是字段覆盖，及只要当前版本的配置文件有这个字段就采用，否则采用全局配置文件里的字段

   onlineConfig/config.json

```json
   {
     //字段可以根据需求添加
     "lastVersionCode" : 251,	//最新版本的版本号
     "minimumRequiredVersion": 251, //如果当前版本号小于此版本，引导强制更新
     "downloadUrl": "url",
     "releaseNotes": "应用更新说明，需要弹框",
     "apkSize": 20.6 ,
     "isUpdateOnlyWifi": true,

     //高级特性,按需添加
     "isDeltaUpdate": false, //增量更新
     "patches":[],	//补丁文件
     "isNeedRemovePatch": false,	//是否需要清除补丁
     "isSilentDownload": false,
     "apkMD5": "md5"
   }
```

   合成规则

```java
   public OnlineConfig merge(OnlineConfig globalConfig) {
           lastVersionCode = lastVersionCode > 0 ? lastVersionCode : globalConfig.lastVersionCode;
           downloadUrl = TextUtils.isEmpty(downloadUrl) ? globalConfig.getDownloadUrl() : downloadUrl;
           releaseNotes = releaseNotes == null ? globalConfig.getReleaseNotes() : releaseNotes;
           minimumRequiredVersion = minimumRequiredVersion > 0 ? minimumRequiredVersion : globalConfig.getMinimumRequiredVersion();
           isNeedRemovePatch = isNeedRemovePatch == null ? globalConfig.getNeedRemovePatch() : isNeedRemovePatch;
           if (patches != null) {
               if (globalConfig.getPatches() != null) {
                   for (String patch : globalConfig.getPatches()) {
                       if (!patches.contains(patch)) {
                           patches.add(patch);
                       }
                   }
               }
           } else {
               patches = globalConfig.getPatches();
           }
           return this;
       }
```

+ 以上做法有几点好处，有全局配置和指定版本配置，就可以方便的实现大部分功能，比如说如果想强制更新所有版本，就可以直接在全局设置修改minimumRequiredVersion和lastVersionCode为最新的版本号，downloadUrl改为最新，当然还得删除指定版本里的这三个字段。

  又比如说如果想增量更新App或者想像指定版本打补丁，这时就需要修改指定版本的配置文件，因为每个版本需要的文件都是独特的

+ 获取到配置文件之后，就可以根据需求写出自己的判断逻辑了，简单的更新逻辑如下所示

```java
if (BuildConfig.VERSION_CODE < config.getMinimumRequiredVersion()) {
       showForceUpdate(context);
} else if (BuildConfig.VERSION_CODE < config.getLastVersionCode()) {
       if (!hasShownUpdateDialog) {
         showUpdateDialog(config);
         hasShownUpdateDialog = true;
       }
 }
```



## 推荐配置

虽然全局配置和版本控制字段一样，理论上可以对随便搭配，但建议所有版本的更新相关的字段都只在全局配置里写，补丁或者增量更新包只在版本配置里写，这样不容易造成混乱。当然如果只需要更新指定版本时一定要特别注意未来的修改



## 关于多渠道更新

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

## 关于如何下载

下载Apk调用的是系统的下载，所以没有静默下载，同时也没有判定重复的机制



## TODO

写个脚本直接生成所需要的文件。。。。

增量更新

上传maven仓库


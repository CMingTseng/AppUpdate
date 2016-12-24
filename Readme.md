# ElevenAppLib

为我的App提供以下通用的功能

- 统一的广告接口
- 统一的About Page
- 自动更新服务
- 应用评分



## Updater(自动更新服务)

结合Github和七牛的app在线更新。

```java
Updater.getInstance(this).setUpdateUrl("https://raw.githubusercontent.com/skyhacker2/skyhacker2.github.com/master/api/apps/AppUpdateDemo/app.json");
Updater.getInstance(this).setDebug(false);
Updater.getInstance(this).checkUpdate();
```

获取的json格式

```json
{
    "versionCode": 2,
    "versionName": "1.1",
    "channels": {
        "_360": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-_360-release.apk",
        "GooglePlay": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-google_play-release.apk",
        "Meizu": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-meizu-release.apk",
        "source": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-source-release.apk",
        "Wandoujia": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-wandoujia-release.apk",
        "Xiaomi": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-xiaomi-release.apk",
        "Yingyongbao": "http://ofjeo4hda.bkt.clouddn.com/AppUpdateDemo/app-yingyongbao-release.apk"
    },
    "updateMessage": "1. 更新界面\n2. 增加在线参数功能\n3. 增加好多功能",
    "onlineParams": {
        "ad": "1",
        "showAd": "true"
    }
}
```

onlineParams用来放在线参数，必须字符串类型。

```java
String ad = OnlineParams.get("ad", "0");
String showAD = OnlineParams.get("showAd", "false");
```

线参数更新广播`Updater.ACTION_ONLINE_PARAMS_UPDATED`

监听广播可以及时获取最新的在线参数


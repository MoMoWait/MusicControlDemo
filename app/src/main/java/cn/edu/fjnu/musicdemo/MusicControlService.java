package cn.edu.fjnu.musicdemo;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MusicControlService extends NotificationListenerService {

    final String TAG = "MusicControlService";

    @Override
    public void onCreate() {
        Log.i(TAG, "音乐推送服务已启动");
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}

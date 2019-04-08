package cn.edu.fjnu.musicdemo;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MusicControlService extends NotificationListenerService {

    final String TAG = "MusicControlService";

    @Override
    public void onCreate() {
        Log.i(TAG, "音乐推送服务已启动");
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "收到通知");
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConstData.BroadCastMsg.NOTIFY_POSTED));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "移除通知");
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConstData.BroadCastMsg.NOTIFY_REMOVED));
    }
}

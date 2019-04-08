package cn.edu.fjnu.musicdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaSessionManager.OnActiveSessionsChangedListener {
    final String TAG = "MainActivity";
    private RecyclerView mRvMusicBrowser;
    private NotifyReceiver mNotifyReceiver = new NotifyReceiver();
    private Handler mHandler = new Handler();
    private MediaSessionManager mediaSessionManager;
    private ComponentName mNotifyReceiveService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNotificationPermission();
        registerListener();
        loadMusicControlAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterListener();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        loadMusicControlAdapter();
    }

    /**
     * 检测通知权限
     */
    private void checkNotificationPermission(){
        String pkgName = getPackageName();
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean isOpen = false;
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        isOpen = true;
                    }
                }
            }
        }
        if(!isOpen)
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    private void initView(){
        mRvMusicBrowser = findViewById(R.id.rv_music_browser);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvMusicBrowser.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData(){
        if(Build.VERSION.SDK_INT >= 21)
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mNotifyReceiveService = new ComponentName(this, MusicControlService.class);
    }

    /**
     * 处理通知
     */
    private void processNotify(){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMusicControlAdapter();
            }
        }, 500);
    }

    /**
     * 加载音乐控制页面
     */
    private void loadMusicControlAdapter(){
        if(Build.VERSION.SDK_INT >= 21){
            List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
            if(mediaControllers.size() > 0){
                List<MusicInfo> musicInfos = new ArrayList<>();
                for(MediaController controller : mediaControllers){
                    try{
                        MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                        MusicInfo itemMusicInfo = new MusicInfo();
                        itemMusicInfo.setAppName(controllerCompat.getPackageName());
                        PlaybackStateCompat playbackStateCompat = controllerCompat.getPlaybackState();
                        itemMusicInfo.setMusicState(playbackStateCompat != null && playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING);
                        itemMusicInfo.setTitle("");
                        MediaMetadataCompat mediaMetadataCompat = controllerCompat.getMetadata();
                        if(mediaMetadataCompat != null){
                            MediaDescriptionCompat descriptionCompat = mediaMetadataCompat.getDescription();
                            if(descriptionCompat != null){
                                String musicTitle = descriptionCompat.getTitle().toString();
                                if(!TextUtils.isEmpty(musicTitle))
                                    itemMusicInfo.setTitle(musicTitle);
                            }
                        }
                        musicInfos.add(itemMusicInfo);
                    }catch (Exception e){
                        //no handle
                        e.printStackTrace();
                    }
                }
                mRvMusicBrowser.setAdapter(new ControlAdapter(this, musicInfos));
            }

        }

    }

    /**
     * 注册监听
     */
    private void registerListener(){
        IntentFilter notifyFilter = new IntentFilter();
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_POSTED);
        notifyFilter.addAction(ConstData.BroadCastMsg.NOTIFY_REMOVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mNotifyReceiver, notifyFilter);
        if(Build.VERSION.SDK_INT >= 21){
            try{
                mediaSessionManager.addOnActiveSessionsChangedListener(this, mNotifyReceiveService);
                List<MediaController> controllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                for(MediaController controller : controllers){
                    MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                    controllerCompat.registerCallback(mediaCompactCallback);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        IntentFilter musicActionFilter = new IntentFilter();
        musicActionFilter.addAction("com.android.music.metachanged");
        musicActionFilter.addAction("com.android.music.playstatechanged");
        musicActionFilter.addAction("com.android.mediacenter.metachanged");
        musicActionFilter.addAction("com.android.mediacenter.playstatechanged");
        musicActionFilter.addAction("com.oppo.music.service.meta_changed");
        musicActionFilter.addAction("com.oppo.music.service.playstate_changed");
        musicActionFilter.addAction("com.miui.player.metachanged");
        musicActionFilter.addAction("com.miui.player.queuechanged");
        registerReceiver(mNotifyReceiver, musicActionFilter);
    }

    /**
     * 取消注册监听
     */
    private void unRegisterListener(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotifyReceiver);
        unregisterReceiver(mNotifyReceiver);
        if(Build.VERSION.SDK_INT >= 21){
            try{
                mediaSessionManager.removeOnActiveSessionsChangedListener(this);
                List<MediaController> controllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                for(MediaController controller : controllers){
                    MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                    controllerCompat.unregisterCallback(mediaCompactCallback);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    class NotifyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            processNotify();
        }
    }

    private  MediaControllerCompat.Callback mediaCompactCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionReady() {
            Log.i(TAG, "onSessionReady");
            processNotify();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            //播放状态发生改变
            loadMusicControlAdapter();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            //播放内容发生改变
            if(metadata != null){
                loadMusicControlAdapter();
            }
        }
    };

}

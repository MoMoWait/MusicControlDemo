package cn.edu.fjnu.musicdemo;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
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
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements MediaSessionManager.OnActiveSessionsChangedListener, OnControlClick {
    final String TAG = "MainActivity";
    private RecyclerView mRvMusicBrowser;
    private NotifyReceiver mNotifyReceiver = new NotifyReceiver();
    private MusicActionReceiver musicActionReceiver = new MusicActionReceiver();
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

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
        switch (view.getId()){
            case R.id.iv_last_music:
                lastMusic((MusicInfo) view.getTag());
                break;
            case R.id.iv_next_music:
                nexMusic((MusicInfo) view.getTag());
                break;
            case R.id.iv_play_pause:
                playOrPause((MusicInfo) view.getTag());
                break;
        }
    }

    private void nexMusic(MusicInfo musicInfo){
        if(isUseAudioManagerKey(musicInfo)){
            Log.i(TAG, "使用audioManger key方式");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
         /*Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         downIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
         sendOrderedBroadcast(downIntent, null);

         Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         upIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
         sendOrderedBroadcast(upIntent, null);*/

         /*new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_MEDIA_NEXT);
            }
        }).start();*/

        }else{
            MediaControllerCompat controllerCompat = findMediaControl(musicInfo);
            if(controllerCompat != null){
                Log.i(TAG, "nextMusic->pkgName:" + controllerCompat.getPackageName());
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if(!isSucc){
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                if(transportControls != null)
                    transportControls.skipToNext();
                }
             }
        }
    }


    private void lastMusic(MusicInfo musicInfo){
        if(isUseAudioManagerKey(musicInfo)){
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        }else{
            MediaControllerCompat controllerCompat = findMediaControl(musicInfo);
            if(controllerCompat != null){
                Log.i(TAG, "lastMusic->pkgName:" + controllerCompat.getPackageName());
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if(!isSucc){
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if(transportControls != null)
                        transportControls.skipToPrevious();
                }
            }
        }

    }


    private void playOrPause(MusicInfo musicInfo){
        if(isUseAudioManagerKey(musicInfo)){
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        }else{
            MediaControllerCompat controllerCompat = findMediaControl(musicInfo);
            boolean isPlay = musicInfo.isMusicState();
            if(controllerCompat != null){
                Log.i(TAG, "playOrPause->pkgName:" + controllerCompat.getPackageName());
                boolean isDown = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.ACTION_DOWN));
                boolean isUp = controllerCompat.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.ACTION_UP));
                boolean isSucc = isDown && isUp;
                if(!isSucc){
                    MediaControllerCompat.TransportControls transportControls = controllerCompat.getTransportControls();
                    if(transportControls != null){
                        if(isPlay)
                            transportControls.pause();
                        else
                            transportControls.play();
                    }

                }
            }
        }

    }


    private boolean isUseAudioManagerKey(MusicInfo musicInfo){
        switch (musicInfo.getPkgName()){
            case "com.tencent.qqmusic":
                return true;
        }
        return false;
    }


    public MediaControllerCompat findMediaControl(MusicInfo musicInfo){
        try{
            List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
            for(MediaController controller : mediaControllers){
                MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                if(musicInfo.getPkgName().equals(controllerCompat.getPackageName()))
                    return controllerCompat;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
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
            try{
                List<MediaController> mediaControllers = mediaSessionManager.getActiveSessions(mNotifyReceiveService);
                if(mediaControllers.size() > 0){
                    List<MusicInfo> musicInfos = new ArrayList<>();
                    for(MediaController controller : mediaControllers){
                        MediaControllerCompat controllerCompat = new MediaControllerCompat(this, MediaSessionCompat.Token.fromToken(controller.getSessionToken()));
                        MusicInfo itemMusicInfo = new MusicInfo();
                        String pkgName = controllerCompat.getPackageName();
                        ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(pkgName, 0);
                        itemMusicInfo.setAppName(applicationInfo.loadLabel(getPackageManager()).toString());
                        itemMusicInfo.setPkgName(pkgName);
                        PlaybackStateCompat playbackStateCompat = controllerCompat.getPlaybackState();
                        itemMusicInfo.setMusicState(playbackStateCompat != null && playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING);
                        itemMusicInfo.setTitle("");
                        MediaMetadataCompat mediaMetadataCompat = controllerCompat.getMetadata();
                        if(mediaMetadataCompat != null){
                            MediaDescriptionCompat descriptionCompat = mediaMetadataCompat.getDescription();
                            if(descriptionCompat != null){
                                CharSequence musicTitle = descriptionCompat.getTitle();
                                if(!TextUtils.isEmpty(musicTitle))
                                    itemMusicInfo.setTitle(musicTitle.toString());
                            }
                        }
                        musicInfos.add(itemMusicInfo);
                    }
                    mRvMusicBrowser.setAdapter(new ControlAdapter(this, musicInfos, this));
                }
            }catch (Exception e){
                e.printStackTrace();
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
        registerReceiver(musicActionReceiver, musicActionFilter);
    }

    /**
     * 取消注册监听
     */
    private void unRegisterListener(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNotifyReceiver);
        unregisterReceiver(musicActionReceiver);
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
            Log.i(TAG, "NotifyReceiver->收到广播:" + intent.getAction());
            processNotify();
        }
    }

    class MusicActionReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "MusicActionReceiver->收到广播:" + intent.getAction());
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

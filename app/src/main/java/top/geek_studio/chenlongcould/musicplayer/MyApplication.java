/*
 * ************************************************************
 * 文件：MyApplication.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月10日 14:49:08
 * 上次修改时间：2018年12月10日 14:47:36
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Utils.NotificationUtils;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    public static boolean FIRST_START = true;

    public static SharedPreferences mDefSharedPreferences;

    private HandlerThread mHandlerThread;

    @Override
    public void onCreate() {
        super.onCreate();

        //监听耳机(有线或无线)的插拔动作, 拔出暂停音乐
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(Data.mMyHeadSetPlugReceiver, intentFilter);

        Data.notificationUtils = new NotificationUtils(this, "Now Playing...");

        mHandlerThread = new HandlerThread("Handler Thread in MainActivity");
        mHandlerThread.start();

        //set language
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);

        mDefSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //first or not
        Values.FIRST_USE = mDefSharedPreferences.getBoolean(Values.SharedPrefsTag.FIRST_USE, true);

        //bg style
        Values.Style.DETAIL_BACKGROUND = mDefSharedPreferences.getString(Values.SharedPrefsTag.DETAIL_BG_STYLE, Values.Style.STYLE_BACKGROUND_BLUR);

        //update style
        Utils.Ui.upDateStyle(mDefSharedPreferences);

        //set play type
        Values.CurrentData.CURRENT_PLAY_TYPE = mDefSharedPreferences.getString(Values.SharedPrefsTag.PLAY_TYPE, Values.TYPE_COMMON);

        Utils.Ui.inDayNightSet(mDefSharedPreferences);
    }

    public final Looper getCustomLooper() {
        return mHandlerThread.getLooper();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: do");

        if (level == TRIM_MEMORY_MODERATE) {
            if (AlbumListFragment.VIEW_HAS_LOAD) {
                Data.sAlbumItems.clear();
                Log.d(TAG, "onTrimMemory: AlbumFragment recycled");
            }
            Data.sCurrentMusicBitmap = null;
        }
        GlideApp.get(this).trimMemory(level);

        super.onTrimMemory(level);
    }

}

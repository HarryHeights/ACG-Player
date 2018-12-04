/*
 * ************************************************************
 * 文件：Utils.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月04日 17:59:25
 * 上次修改时间：2018年12月04日 17:59:12
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Utils;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

@SuppressWarnings("WeakerAccess")
public final class Utils {

    public static class Audio {
        private final static MediaMetadataRetriever sMediaMetadataRetriever = new MediaMetadataRetriever();
        private static final String TAG = "Audio";

        /**
         * 检测播放器是否准备完毕 (默认进app 为true)
         */
        private static volatile boolean READY = true;

        /**
         * 获取封面
         *
         * @param mediaUri mp3 path
         */
        @Nullable
        public static Bitmap getMp3Cover(@NonNull String mediaUri) {

//            //检测不支持封面的音乐类型
//            if (mediaUri.contains("ogg") || mediaUri.contains("flac")) {
//                return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_audiotrack_24px);
//            }

            sMediaMetadataRetriever.setDataSource(mediaUri);
            byte[] picture = sMediaMetadataRetriever.getEmbeddedPicture();
            if (picture != null) {
                return BitmapFactory.decodeByteArray(picture, 0, picture.length);
            } else {
                Log.d(TAG, "getMp3Cover: return def");
                return BitmapFactory.decodeResource(Data.sActivities.get(0).getResources(), R.drawable.ic_audiotrack_24px);
            }
        }

        public static byte[] getAlbumByteImage(@NonNull String path) {
            sMediaMetadataRetriever.setDataSource(path);
            return sMediaMetadataRetriever.getEmbeddedPicture();
        }

    }

    public static class Ui {

        private static final String TAG = "Ui";

        public static int POSITION = 200;

        public static void inDayNightSet(SharedPreferences sharedPreferences) {
            if (sharedPreferences.getBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false)) {
                Values.Color.TEXT_COLOR = Values.Color.TEXT_COLOR_IN_NIGHT;
            } else {
                Values.Color.TEXT_COLOR = Values.Color.TEXT_COLOR_IN_DAY;
            }
        }

        /**
         * 获取导航栏高度
         *
         * @param context context
         * @return nav height
         */
        public static int getNavheight(Context context) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int height = resources.getDimensionPixelSize(resourceId);
            Log.v("dbw", "Navi height:" + height);
            return height;
        }

        /**
         * set color (style)
         *
         * @param context      context
         * @param toolBarColor appBarLayout
         * @param toolbar      toolbar
         */
        public static void setAppBarColor(Activity context, AppBarLayout toolBarColor, Toolbar toolbar) {
            SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            toolBarColor.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            context.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        }

        public static void setAppBarColor(Activity context, AppBarLayout toolBarColor, android.support.v7.widget.Toolbar toolbar) {
            SharedPreferences mDefPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            toolBarColor.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            toolbar.setBackgroundColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_COLOR, Color.parseColor("#008577")));
            context.getWindow().setNavigationBarColor(mDefPrefs.getInt(Values.ColorInt.PRIMARY_DARK_COLOR, Color.parseColor("#00574B")));
        }

        public static void setPlayButtonNowPlaying() {
            if (Data.sActivities.size() != 0) {
                MainActivity activity = (MainActivity) Data.sActivities.get(0);
                activity.runOnUiThread(() -> activity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.SET_BUTTON_PLAY));
            }
        }

        public static AlertDialog createMessageDialog(@NonNull Activity context, @NonNull String title, @NonNull String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(true);
            return builder.create();
        }

        public static void fastToast(@NonNull Context context, @NonNull String content) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }

        /**
         * 获取图片亮度
         *
         * @param bm bitmap
         */
        public static int getBright(@NonNull Bitmap bm) {
            int width = bm.getWidth() / 4;
            int height = bm.getHeight() / 4;
            int r, g, b;
            int count = 0;
            int bright = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    count++;
                    int localTemp = bm.getPixel(i, j);
                    r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                    g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                    b = (localTemp | 0xffffff00) & 0x0000ff;
                    bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
                }
            }
            return bright / count;
        }

        public static boolean ANIMATION_IN_DETAIL_DONE = true;

        /**
         * 设置背景与动画 (blur style)
         *
         * @param activity if use fragment may case {@link java.lang.NullPointerException}, glide will call {@link Fragment#getActivity()}
         */
        public static void setBlurEffect(@NonNull MainActivity activity, @NonNull byte[] bitmap, @NonNull ImageView primaryBackground, @NonNull ImageView primaryBackgroundBef, TextView nextText) {

            new Handler(Looper.getMainLooper()).post(() -> {
                ANIMATION_IN_DETAIL_DONE = false;
                primaryBackground.setVisibility(View.VISIBLE);

                Palette.from(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length)).generate(p -> {
                    if (p != null) {
//                        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, p.getVibrantColor(Color.parseColor(Values.Color.NOT_VERY_BLACK)));
//                        animator.setDuration(300);
//                        animator.addUpdateListener(animation -> nextText.setTextColor((Integer) animation.getAnimatedValue()));
//                        animator.start();
                        nextText.setTextColor(p.getVibrantColor(Color.parseColor(Values.Color.TEXT_COLOR)));
                    }
                });

                //clear
                GlideApp.with(activity).clear(primaryBackgroundBef);
                if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                    primaryBackgroundBef.post(() -> GlideApp.with(activity)
                            .load(bitmap)
                            .dontAnimate()
                            .apply(bitmapTransform(new BlurTransformation(20, 30)))
                            .into(primaryBackgroundBef));
                } else {
                    Palette.from(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length)).generate(p -> {
                        if (p != null) {
                            primaryBackgroundBef.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                        }
                    });

                }

                Animator animator = ViewAnimationUtils.createCircularReveal(
                        primaryBackgroundBef, primaryBackgroundBef.getWidth() / 2, POSITION,
                        0,
                        (float) Math.hypot(primaryBackgroundBef.getWidth(), primaryBackgroundBef.getHeight()));

                animator.setInterpolator(new AccelerateInterpolator());
                animator.setDuration(700);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GlideApp.with(activity).clear(primaryBackground);
                        if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                            GlideApp.with(activity)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(new BlurTransformation(20, 30)))
                                    .into(primaryBackground);
                        } else {
                            Palette.from(BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length)).generate(p -> {
                                if (p != null) {
                                    primaryBackground.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                                }
                            });
                        }

                        primaryBackground.setVisibility(View.GONE);
                        ANIMATION_IN_DETAIL_DONE = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            });
        }

        public static void setBlurEffect(@NonNull MainActivity activity, @NonNull Bitmap bitmap, @NonNull ImageView primaryBackground, @NonNull ImageView primaryBackgroundBef, TextView nextText) {

            new Handler(Looper.getMainLooper()).post(() -> {
                ANIMATION_IN_DETAIL_DONE = false;
                primaryBackground.setVisibility(View.VISIBLE);

                Palette.from(bitmap).generate(p -> {
                    if (p != null) {
//                        nextText.clearAnimation();
//                        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, p.getVibrantColor(Color.parseColor(Values.Color.NOT_VERY_BLACK)));
//                        animator.setDuration(300);
//                        animator.addUpdateListener(animation -> nextText.setTextColor((Integer) animation.getAnimatedValue()));
                        nextText.setTextColor(p.getVibrantColor(Color.parseColor(Values.Color.TEXT_COLOR)));
                    }
                });

                //clear
                GlideApp.with(activity).clear(primaryBackgroundBef);
                if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                    primaryBackgroundBef.post(() -> GlideApp.with(activity)
                            .load(bitmap)
                            .dontAnimate()
                            .apply(bitmapTransform(new BlurTransformation(20, 30)))
                            .into(primaryBackgroundBef));
                } else {
                    Palette.from(bitmap).generate(p -> {
                        if (p != null) {
                            primaryBackgroundBef.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                        }
                    });
                }

                Animator animator = ViewAnimationUtils.createCircularReveal(
                        primaryBackgroundBef, primaryBackgroundBef.getWidth() / 2, POSITION,
                        0,
                        (float) Math.hypot(primaryBackgroundBef.getWidth(), primaryBackgroundBef.getHeight()));

                animator.setInterpolator(new AccelerateInterpolator());
                animator.setDuration(700);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GlideApp.with(activity).clear(primaryBackground);
                        if (Values.Style.DETAIL_BACKGROUND.equals(Values.Style.STYLE_BACKGROUND_BLUR)) {
                            GlideApp.with(activity)
                                    .load(bitmap)
                                    .dontAnimate()
                                    .apply(bitmapTransform(new BlurTransformation(20, 30)))
                                    .into(primaryBackground);
                        } else {
                            Palette.from(bitmap).generate(p -> {
                                if (p != null) {
                                    primaryBackground.setBackgroundColor(p.getVibrantColor(Color.TRANSPARENT));
                                }
                            });
                        }

                        primaryBackground.setVisibility(View.GONE);
                        ANIMATION_IN_DETAIL_DONE = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            });
        }

        public static boolean isColorLight(@ColorInt int color) {
            double darkness = 1.0D - (0.299D * (double) Color.red(color) + 0.587D * (double) Color.green(color) + 0.114D * (double) Color.blue(color)) / 255.0D;
            return darkness < 0.4D;
        }

        public static void upDateStyle(SharedPreferences mDefSharedPreferences) {
            if (mDefSharedPreferences.getBoolean(Values.SharedPrefsTag.AUTO_NIGHT_MODE, false)) {
                Values.Style.NIGHT_MODE = true;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Values.Color.TEXT_COLOR = "#7c7c7c";
            } else {
                Values.Style.NIGHT_MODE = false;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Values.Color.TEXT_COLOR = "#3c3c3c";
            }
        }
    }

    public static final class HandlerSend {

    }

    /**
     * start activity, broadcast, service...
     */
    public static final class SendSomeThing {

        public static final String TAG = "SendSomeThing";

        /**
         * send broadcast by pause
         *
         * @param context context
         */
        public static void sendPause(Context context) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPause));
            context.sendBroadcast(intent, Values.Permission.BROAD_CAST);
        }

        public static void sendPlay(Context context, int playType) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), Values.BroadCast.ReceiverOnMusicPlay));
            intent.putExtra("play_type", playType);
            context.sendBroadcast(intent, Values.Permission.BROAD_CAST);
        }
    }

}

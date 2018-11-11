/*
 * ************************************************************
 * 文件：MainActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月06日 07:32:30
 * 上次修改时间：2018年11月05日 19:45:55
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import top.geek_studio.chenlongcould.musicplayer.Adapters.MyPagerAdapter;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.AlbumListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Service.MyMusicService;
import top.geek_studio.chenlongcould.musicplayer.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public final class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private boolean TOOLBAR_CLICKED = false;

    private SharedPreferences mDefaultSpf;

    private List<Fragment> mFragmentList = new ArrayList<>();

    private NotLeakHandler mHandler;

    private HandlerThread mHandlerThread;

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private MyPagerAdapter mPagerAdapter;

    private ArrayList<String> mTitles = new ArrayList<>();

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private ImageView mNavHeaderImageView;

    private Menu mMenu;

    /**
     * ----------------- fragment(s) ----------------------
     */
    private MusicListFragment mMusicListFragment;

    private AlbumListFragment mAlbumListFragment;

    /**
     * ----------------- playing info ---------------------
     */
    private TextView mNowPlayingSongText;

    private ImageView mNowPlayingSongImage;

    private TextView mNowPlayingSongAlbumText;

    private ImageView mNowPlayingStatusImage;

    private ImageView mNowPlayingBackgroundImage;

    //Body
    private ConstraintLayout mNowPlayingBody;

    /**
     * onXXX
     * Override
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");

        mHandlerThread = new HandlerThread("Handler Thread in MainActivity");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        //service
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent);
        bindService(new Intent(this, MyMusicService.class), Data.sServiceConnection, BIND_AUTO_CREATE);

        initData();

        initView();

        reLoadInfoBar();

    }

    @Override
    protected void onDestroy() {
        Data.sActivities.remove(this);
        mHandlerThread.quitSafely();
        unbindService(Data.sServiceConnection);
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_toolbar, mMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toolbar_exit: {
                finish();
            }
            break;

            /*--------------- 快速 随机 播放 ----------------*/
            case R.id.menu_toolbar_fast_play: {
                Data.sHistoryPlayIndex.clear();
                Utils.Audio.shufflePlayback();
            }
            break;
        }
        return true;
    }

    /**
     * when activity finished but service not(music playing in background),
     * the method can reload the music being played info to infoBar
     */
    private void reLoadInfoBar() {
        if (Data.sMusicBinder != null) {
            Log.d(TAG, "initData: not null");
            if (Data.sMusicBinder.isPlayingMusic()) {
                setCurrentSongInfo(Data.sCurrentMusicName, Data.sCurrentMusicAlbum, Values.CURRENT_SONG_PATH, Data.sCurrentMusicBitmap, "reload");
                setButtonTypePlay();
            }
        }
    }

    /**
     * init Something
     */
    private void initData() {
        Data.sActivities.add(this);

        mDefaultSpf = PreferenceManager.getDefaultSharedPreferences(this);

        new Thread(() -> {
            String tab_1 = "歌曲";
            mTitles.add(tab_1);
            mMusicListFragment = MusicListFragment.newInstance();
            mFragmentList.add(mMusicListFragment);

            String tab_2 = "专辑";
            mTitles.add(tab_2);
            mAlbumListFragment = AlbumListFragment.newInstance();
            mFragmentList.add(mAlbumListFragment);

            String tab_3 = "播放列表";
            mTitles.add(tab_3);
            mFragmentList.add(PlayListFragment.newInstance(2));

            Message message = Message.obtain();
            Bundle bundle = new Bundle();
            ArrayList<String> titles = new ArrayList<>();
            titles.add(tab_1);
            titles.add(tab_2);
            titles.add(tab_3);
            bundle.putStringArrayList("titles", titles);
            message.what = 899;
            message.setData(bundle);

            mHandler.sendMessage(message);
        }).start();

    }

    private void initView() {
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.activity_main_tool_bar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar");
        } else {

            //根据recycler view的滚动程度, 来判断如何返回顶部
            mToolbar.setOnClickListener(v -> {
                if (TOOLBAR_CLICKED) {
                    switch (Values.CURRENT_PAGE_INDEX) {
                        case 0: {
                            if (Values.CURRENT_BIND_INDEX_MUSIC_LIST > 20) {
                                mMusicListFragment.getRecyclerView().scrollToPosition(0);
                            } else {
                                mMusicListFragment.getRecyclerView().smoothScrollToPosition(0);
                            }
                        }
                        break;
                        case 1: {
                            if (Values.CURRENT_BIND_INDEX_ALBUM_LIST > 20) {
                                mAlbumListFragment.getRecyclerView().scrollToPosition(0);
                            } else {
                                mAlbumListFragment.getRecyclerView().smoothScrollToPosition(0);
                            }
                        }
                    }

                }
                TOOLBAR_CLICKED = true;
                new Handler().postDelayed(() -> TOOLBAR_CLICKED = false, 1000);

            });
            setSupportActionBar(mToolbar);
        }

        mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);
        if (mDrawerLayout != null) {
            mNavigationView = findViewById(R.id.activity_main_nav_view);
            if (mNavigationView == null) {
                throw new IllegalStateException("Layout requires a NavigationView");
            } else {
                mNavHeaderImageView = mNavigationView.getHeaderView(0).findViewById(R.id.nav_view_image);
                mNavHeaderImageView.setOnClickListener(v -> {
                    mDrawerLayout.closeDrawers();
                    if (Values.HAS_PLAYED) {
                        startActivity(new Intent(MainActivity.this, MusicDetailActivity.class));
                    } else {
                        Utils.Ui.fastToast(MainActivity.this, "No music playing.");
                    }
                });
            }
        }

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                return true;
            }
        });

        mNowPlayingSongAlbumText = findViewById(R.id.activity_main_now_playing_album_name);
        mNowPlayingBody = findViewById(R.id.current_info);
        mNowPlayingStatusImage = findViewById(R.id.activity_main_info_bar_status_image);
        mNowPlayingBackgroundImage = findViewById(R.id.current_info_background);

        mNowPlayingStatusImage.setOnClickListener(v -> {
            //判断是否播放过, 如没有默认随机播放
            if (Values.HAS_PLAYED) {
                if (Values.MUSIC_PLAYING) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPause));
                    sendBroadcast(intent);

                } else {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(Values.PKG_NAME, Values.BroadCast.ReceiverOnMusicPlay));
                    intent.putExtra("play_type", 3);
                    sendBroadcast(intent);
                }
            } else {
                Toast.makeText(MainActivity.this, "Shuffle Playback!", Toast.LENGTH_SHORT).show();
                Data.sHistoryPlayIndex.clear();
                Utils.Audio.shufflePlayback();
            }
        });

        mNowPlayingBody.setOnClickListener(v -> {
            if (Values.HAS_PLAYED) {
                Intent intent = new Intent(MainActivity.this, MusicDetailActivity.class);
                intent.putExtra("intent_args", "by_clicked_body");
                startActivity(intent);
            } else {
                Utils.Ui.fastToast(MainActivity.this, "No music playing.");
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24px);
        }

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mNowPlayingSongText = findViewById(R.id.activity_main_now_playing_name);
        mNowPlayingSongImage = findViewById(R.id.recycler_item_clover_image);

        // 实例代码
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                ArgbEvaluator evaluator = new ArgbEvaluator(); // ARGB求值器
//
//                final int green = 0xFF43CD80, blue = 0xFF11A0F8, yellow = 0xFFFFBB43, red = 0xFFB54B36;
//
//                int evaluate; // 初始默认颜色
//                if (position == 0) {
//                    evaluate = (Integer) evaluator.evaluate(positionOffset, green, blue); // 根据positionOffset和第0页~第1页的颜色转换范围取颜色值
//                } else if (position == 1) {
//                    evaluate = (Integer) evaluator.evaluate(positionOffset, blue, yellow); // 根据positionOffset和第1页~第2页的颜色转换范围取颜色值
//                } else if (position == 2) {
//                    evaluate = (Integer) evaluator.evaluate(positionOffset, yellow, red); // 根据positionOffset和第2页~第3页的颜色转换范围取颜色值
//                } else {
//                    evaluate = red; // 最终第3页的颜色
//                }
//
//                mToolbar.setBackgroundColor(evaluate);
//                mTabLayout.setBackgroundColor(evaluate);
//                mStatusView.setBackgroundColor(evaluate);
            }

            @Override
            public void onPageSelected(int position) {
                Values.CURRENT_PAGE_INDEX = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initDrawerToggle();

        //final
        Data.sDefTextColorStateList = mNowPlayingSongText.getTextColors();
        Data.sDefIcoColorStateList = mNowPlayingStatusImage.getImageTintList();
    }

    private void initDrawerToggle() {
        // 参数：开启抽屉的activity、DrawerLayout的对象、toolbar按钮打开关闭的对象、描述open drawer、描述close drawer
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        // 添加抽屉按钮，通过点击按钮实现打开和关闭功能; 如果不想要抽屉按钮，只允许在侧边边界拉出侧边栏，可以不写此行代码
        mDrawerToggle.syncState();
        // 设置按钮的动画效果; 如果不想要打开关闭抽屉时的箭头动画效果，可以不写此行代码
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    /**
     * set Info (auto put in data.)
     *
     * @param songName  music name
     * @param albumName music album name
     * @param songPath  music path
     * @param cover     music cover image, it is @NullAble(some types of music do not have cover)
     * @param args      oth params(if "reload", do not need to set InfoBar again)
     */
    public void setCurrentSongInfo(String songName, String albumName, String songPath, @Nullable Bitmap cover, String... args) {

        //if already set the same info(from path) return
        if (Values.CURRENT_SONG_PATH.equals(songPath) && args != null) {
            return;
        }

        runOnUiThread(() -> {

            mNowPlayingSongText.setText(songName);
            mNowPlayingSongAlbumText.setText(albumName);

            if (cover != null) {
                //color set
                int currentBright = Utils.Ui.getBright(cover);
                Log.d(TAG, "---------------------setCurrentSongInfo: get bright " + currentBright);
                GlideApp.with(MainActivity.this).load(cover).transition(DrawableTransitionOptions.withCrossFade()).override(150, 150).into(mNowPlayingSongImage);
                GlideApp.with(MainActivity.this).load(cover).transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(mNavHeaderImageView);

                if (currentBright > (255 / 2)) {
                    mNowPlayingSongText.setTextColor(Data.sDefTextColorStateList);
                    mNowPlayingSongAlbumText.setTextColor(Data.sDefTextColorStateList);
                    mNowPlayingStatusImage.setImageTintList(Data.sDefIcoColorStateList);
                } else {
                    mNowPlayingSongText.setTextColor(Color.WHITE);
                    mNowPlayingSongAlbumText.setTextColor(Color.WHITE);
                    mNowPlayingStatusImage.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                }

                GlideApp.with(MainActivity.this).load(cover)
                        .transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
                        .apply(bitmapTransform(new BlurTransformation(30, 20)))
                        .override(100, 100)
                        .into(mNowPlayingBackgroundImage);
            }

            setButtonTypePlay();
        });
    }

    public void setButtonTypePause() {
        runOnUiThread(() -> mNowPlayingStatusImage.setImageResource(R.drawable.ic_play_arrow_black_24dp));
    }

    public void setButtonTypePlay() {
        runOnUiThread(() -> mNowPlayingStatusImage.setImageResource(R.drawable.ic_pause_white_24dp));
    }

    public void setToolbarSubTitle(String text) {
        mToolbar.setSubtitle(text);
    }

    /**
     * getter
     */
    public List<Fragment> getFragmentList() {
        return mFragmentList;
    }

    public ConstraintLayout getNowPlayingBody() {
        return mNowPlayingBody;
    }

    public NotLeakHandler getHandler() {
        return mHandler;
    }

    class NotLeakHandler extends Handler {
        private WeakReference<MainActivity> mWeakReference;

        NotLeakHandler(MainActivity activity, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 899: {
                    ArrayList<String> titles = msg.getData().getStringArrayList("titles");
                    if (titles != null) {
                        runOnUiThread(() -> {
                            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
                            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
                            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));

                            mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
                            mViewPager.setOffscreenPageLimit(2);
                            mTabLayout.setupWithViewPager(mViewPager);
                            mViewPager.setCurrentItem(0);
                            mViewPager.setAdapter(mPagerAdapter);
                            Values.CURRENT_PAGE_INDEX = 0;
                        });
                    }
                }
                break;
                default:
            }
        }
    }
}

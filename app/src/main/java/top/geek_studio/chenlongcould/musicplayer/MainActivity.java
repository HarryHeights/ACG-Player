/*
 * ************************************************************
 * 文件：MainActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年11月05日 17:54:16
 * 上次修改时间：2018年11月05日 17:53:36
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

    /**----------------- playing info ---------------------*/
    private TextView mNowPlayingSongText;

    private ImageView mNowPlayingSongImage;

    private TextView mNowPlayingSongAlbumText;

    /** --------------------- Media Player ----------------------*/
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPermission();

        initView();

        initData();

        mHandlerThread = new HandlerThread("Handler Thread in MainActivity");
        mHandlerThread.start();
        mHandler = new NotLeakHandler(this, mHandlerThread.getLooper());

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragmentList, mTitles);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(0);
        Values.CURRENT_PAGE_INDEX = 0;

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                Values.CURRENT_PAGE_INDEX = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Values.REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Values.REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fastToast("Succeed to get permission!");
                } else {
                    initPermission();
                    fastToast("Failed to get permission, again!");
                }
            }
        }
    }

    private void fastToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void initData() {

        String tab_1 = "歌曲";
        mTabLayout.addTab(mTabLayout.newTab().setText(tab_1));
        mTitles.add(tab_1);
        mFragmentList.add(MusicListFragment.newInstance(0));

        String tab_2 = "专辑";
        mTabLayout.addTab(mTabLayout.newTab().setText(tab_2));
        mTitles.add(tab_2);
        mFragmentList.add(PublicFragment.newInstance(1));

        String tab_3 = "播放列表";
        mTabLayout.addTab(mTabLayout.newTab().setText(tab_3));
        mTitles.add(tab_3);
        mFragmentList.add(PublicFragment.newInstance(2));

    }

    private void initView() {
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.tool_bar);
        mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);
        mNowPlayingSongAlbumText = findViewById(R.id.activity_main_now_playing_album_name);
        mNavigationView = findViewById(R.id.activity_main_nav_view);
        mNavHeaderImageView = mNavigationView.getHeaderView(0).findViewById(R.id.nav_view_image);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24px);
        }

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mNowPlayingSongText = findViewById(R.id.activity_main_now_playing_name);
        mNowPlayingSongImage = findViewById(R.id.recycler_item_clover_image);

        initDrawerToggle();
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setCurrentSongInfo(String songName, String albumName, Bitmap cover) {
        mNowPlayingSongText.setText(songName);
        mNowPlayingSongAlbumText.setText(albumName);
        GlideApp.with(this).load(cover).into(mNowPlayingSongImage);
        GlideApp.with(this).load(cover).centerCrop().into(mNavHeaderImageView);
    }

    public void setToolbarSubTitle(String text) {
        mToolbar.setSubtitle(text);
    }

    private void initDrawerToggle() {
        // 参数：开启抽屉的activity、DrawerLayout的对象、toolbar按钮打开关闭的对象、描述open drawer、描述close drawer
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close);
        // 添加抽屉按钮，通过点击按钮实现打开和关闭功能; 如果不想要抽屉按钮，只允许在侧边边界拉出侧边栏，可以不写此行代码
        mDrawerToggle.syncState();
        // 设置按钮的动画效果; 如果不想要打开关闭抽屉时的箭头动画效果，可以不写此行代码
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toolbar_exit: {
                finish();
            }
            break;
        }
        return true;
    }

    class NotLeakHandler extends Handler {
        private WeakReference<MainActivity> mWeakReference;

        NotLeakHandler(MainActivity activity,Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
            }
        }
    }

    @Override
    protected void onDestroy() {
        mHandlerThread.quitSafely();
        super.onDestroy();
    }
}

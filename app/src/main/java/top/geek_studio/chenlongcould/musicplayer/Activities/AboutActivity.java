/*
 * ************************************************************
 * 文件：AboutActivity.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2019年01月27日 13:11:38
 * 上次修改时间：2019年01月19日 12:17:57
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.MyApplication;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutTemplateDulTextBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutTemplateSingleTextBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.AboutTemplateThanksBinding;
import top.geek_studio.chenlongcould.musicplayer.databinding.ActivityAboutAppBinding;

public class AboutActivity extends MyBaseCompatActivity {

    private static final String TAG = "AboutActivity";

    private ActivityAboutAppBinding mAppBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_app);

        mAppBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        final AboutTemplateDulTextBinding version = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateDulTextBinding author = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateDulTextBinding web = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_dul_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateSingleTextBinding update = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateSingleTextBinding mail = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateSingleTextBinding lic = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateSingleTextBinding share = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_single_text, (ViewGroup) mAppBinding.getRoot(), false);
        final AboutTemplateThanksBinding thanksBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.about_template_thanks, (ViewGroup) mAppBinding.getRoot(), false);

        version.ico.setImageResource(R.drawable.ic_info_outline_black_24dp);
        version.mainText.setText(getString(R.string.version));
        try {
            version.subText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        update.ico.setImageResource(R.drawable.ic_cloud_download_black_24dp);
        update.text.setText(getString(R.string.update_log));
        update.body.setOnClickListener(v -> {
            AlertDialog load = Utils.Ui.getLoadingDialog(AboutActivity.this, "Loading...");
            load.show();

            final AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
            builder.setCancelable(true);
            builder.setTitle(getString(R.string.update_log));
            builder.setPositiveButton(getString(R.string.close), (dialog, which) -> dialog.cancel());

            final HttpUtil httpUtil = new HttpUtil();

            httpUtil.sedOkHttpRequest("https://www.coolapk.com/apk/top.geek_studio.chenlongcould.musicplayer.Common", new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(AboutActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    StringBuilder responseText;
                    if (response.body() != null) {
                        responseText = new StringBuilder(response.body().string());
                        final Document document = Jsoup.parse(responseText.toString());
                        final Elements elements = document.getElementsByClass("apk_left_title_info");
                        builder.setMessage(elements.get(0).text());

                        runOnUiThread(() -> {
                            load.dismiss();
                            builder.show();
                        });

                    } else {
                        Toast.makeText(AboutActivity.this, "Get Data Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        });

        lic.text.setText(getString(R.string.licenses));
        lic.ico.setImageResource(R.drawable.ic_find_in_page_black_24dp);
        lic.body.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, AboutLic.class)));

        share.text.setText(getString(R.string.share));
        share.ico.setImageResource(R.drawable.ic_share_black_24dp);
        share.body.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, getResources()
                        .getString(R.string.app_name) + "\r\n" + "https://www.coolapk.com/apk/top.geek_studio.chenlongcould.musicplayer.Common")));

        author.mainText.setText(getString(R.string.geek_aug));
        author.subText.setText(getString(R.string.jiangsu));
        author.ico.setImageResource(R.drawable.ic_person_black_24dp);

        mail.ico.setImageResource(R.drawable.ic_mail_outline_black_24dp);
        mail.text.setText(getString(R.string.send_a_email));
        mail.body.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:827266641@qq.com"));
            intent.putExtra(Intent.EXTRA_EMAIL, "827266641@qq.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            startActivity(Intent.createChooser(intent, "E-Mail"));
        });

        web.ico.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        web.mainText.setText(getString(R.string.open_blog));
        web.subText.setText(MyApplication.MY_WEB_SITE);
        web.aboutItemVer.setOnClickListener(v -> {
            Uri uri = Uri.parse(MyApplication.MY_WEB_SITE);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });

        mAppBinding.card1.addView(version.getRoot());
        mAppBinding.card1.addView(update.getRoot());
        mAppBinding.card1.addView(lic.getRoot());
        mAppBinding.card1.addView(share.getRoot());

        mAppBinding.card2.addView(author.getRoot());
        mAppBinding.card2.addView(mail.getRoot());
        mAppBinding.card2.addView(web.getRoot());

    }
}

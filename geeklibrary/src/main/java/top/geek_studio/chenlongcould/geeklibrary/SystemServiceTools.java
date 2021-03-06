/*
 * ************************************************************
 * 文件：SystemServiceTools.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月17日 17:31:47
 * 上次修改时间：2019年01月17日 17:29:00
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_studio.chenlongcould.geeklibrary;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * @author chenlongcould
 */
public class SystemServiceTools {
    public static void putTextIntoClip(@NonNull Context context, @NonNull String content) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象
        ClipData clipData = ClipData.newPlainText("vmess", content);
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData);
    }
}

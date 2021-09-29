package com.g.rtmp_screen_live;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ScreenLive implements Runnable{
    private String url;
    private MediaProjectionManager manager;
    private MediaProjection mediaProjection;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startLive(String url, Activity activity) {
        this.url = url;
        manager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = manager.createScreenCaptureIntent();
        activity.startActivityForResult(screenCaptureIntent,100);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            mediaProjection = manager.getMediaProjection(resultCode, data);
            // 连接rtmp服务器
            // 编码-》发送
            LiveExecutors.getInstance().execute(this);
        }
    }

    @Override
    public void run() {
        if (!connect(url)){
            return;
        }

    }

    private native boolean connect(String url);
}

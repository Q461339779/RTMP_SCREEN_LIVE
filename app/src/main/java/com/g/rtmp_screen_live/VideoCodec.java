package com.g.rtmp_screen_live;

import android.annotation.SuppressLint;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoCodec extends Thread {
    MediaCodec mediaCodec;
    boolean isLiveing;
    long timestamp;
    MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startLive(MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;
        isLiveing = true;
        //媒体数据格式信息
        MediaFormat mediaformat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 360, 640);
        // 码率
        mediaformat.setInteger(MediaFormat.KEY_BIT_RATE, 400_000);
        //帧率
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        // 关键帧间隔  太长影响开屏时间  延迟   太短数据量过大
        mediaformat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
        // 数据源的格式
        mediaformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //创建支持h264 的编码器
        try {
            //自动从 inputSurface 获取数据进行编码
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec.configure(mediaformat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            //创建离屏画布
            Surface inputSurface = mediaCodec.createInputSurface();
            //数据给到 inputSurface
            mediaProjection.createVirtualDisplay(
                    "abc",360,640,1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    inputSurface,null,null
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        //启动mediaCodec  编码器  进行编码
        mediaCodec.start();
        //拿到数据
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        //手动刷新关键帧
        while (timestamp !=0){
            if (System.currentTimeMillis() - timestamp >= 2_000){
                Bundle bundle = new Bundle();
                bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,0);
                mediaCodec.setParameters(bundle);
                timestamp = System.currentTimeMillis();
            }else {
                timestamp = System.currentTimeMillis();
            }

            //获取编码之后的数据
            //从输出队列获取输出数据 下标
            //-1编码没有完成 -2 数据格式改变 -3
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo,10);
            if (index>=0){
                //成功取出了编码数据
                ByteBuffer buffer = mediaCodec.getOutputBuffer(index);
                byte[] data = new byte[bufferInfo.size];
                buffer.get(data);

                //todo 编码好之后的数据送去按照rtmp格式封包，再发送出去


                //释放,让队列中index位置能放新数据
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
        isLiveing = false;
        mediaCodec.stop();
        mediaCodec.release();
        virtualDisplay.release();
        mediaProjection.stop();

    }
}

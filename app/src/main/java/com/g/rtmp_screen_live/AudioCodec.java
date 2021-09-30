package com.g.rtmp_screen_live;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioCodec extends Thread{
    private final ScreenLive screenLive;
    MediaCodec mediaCodec;
    private AudioRecord audioRecord;
    boolean isRecording;
    private int minBufferSize;
    long startTime;

    public void startLive() {
        /**
         * 准备编码器
         */
        try {
            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,44100,1);
            //编码规格，可以看成质量
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //码率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64_000);
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //创建AudioRecord 录音
        //最小缓冲区大小
        minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        start();


    }

    public AudioCodec(ScreenLive screenLive) {
        this.screenLive = screenLive;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {

        isRecording = true;
        mediaCodec.start();

        byte[] buffer = new byte[minBufferSize];
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isRecording){
            //得到采集的声音数据
            int len = audioRecord.read(buffer,0,buffer.length);
            if (len<0){
                continue;
            }
            //读到数据交给编码器编码
            int index = mediaCodec.dequeueInputBuffer(0);
            if (index>=0){
                ByteBuffer byteBuffer = mediaCodec.getInputBuffer(index);
                byteBuffer.clear();
                //把输入塞入容器
                byteBuffer.put(buffer, 0, len);

                //通知容器我们使用完了，你可以拿去编码了
                // 时间戳： 微秒， nano纳秒/1000
                mediaCodec.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0);

            }
        }
    }
}

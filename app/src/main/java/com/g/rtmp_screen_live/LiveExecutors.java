package com.g.rtmp_screen_live;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LiveExecutors {
    private static volatile LiveExecutors instance;

    // cpu 数量
    private static final int  CPU_COUNT = Runtime.getRuntime().availableProcessors();
    // 核心线程数
    private static final int CORE_POOL_SIZE = Math.max(2,Math.min(CPU_COUNT-1,4));
    // 最大线程数
    private static final int MAXIMUN_POOL_SIZE = CPU_COUNT * 2 + 1;
    // 保活时间
    private static final int KEEP_ALIVE_SECONDS = 30;
    // 任务队列
    private static final BlockingQueue<Runnable> sPoolWorkQueue =
             new LinkedBlockingQueue<Runnable>(5);

    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR = null;
    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUN_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                sPoolWorkQueue
        );

        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    private LiveExecutors(){

    }

    public static  LiveExecutors getInstance(){
        if (instance == null){
            synchronized (LiveExecutors.class){
                if (instance == null){
                    instance = new LiveExecutors();
                }
            }
        }
        return instance;
    }

    public void execute(Runnable runnable){
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

}

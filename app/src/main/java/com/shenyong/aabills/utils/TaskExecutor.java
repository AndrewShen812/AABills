package com.shenyong.aabills.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ShenYong
 * @date 2019/4/3
 */
public class TaskExecutor {

    private static final int THREAD_COUNT = 5;

    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    private TaskExecutor(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    private TaskExecutor() {
        this(new DiskIoThreadExecutor(),
            new ThreadPoolExecutor(THREAD_COUNT, 100, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(256), new NamedThreadFactory(), new ThreadPoolExecutor.AbortPolicy()),
            new MainThreadExecutor());
    }

    private static class TaskExecutorHolder {
        private static final TaskExecutor INSTANCE = new TaskExecutor();
    }

    private static TaskExecutor getInstance() {
        return TaskExecutorHolder.INSTANCE;
    }

    public static Executor diskIO() {
        return getInstance().diskIO;
    }

    public static Executor networkIO() {
        return getInstance().networkIO;
    }

    public static Executor mainThread() {
        return getInstance().mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }


    /**
     * Executor that runs a task on a new background thread.
     */
    private static class DiskIoThreadExecutor implements Executor {

        private final ThreadPoolExecutor mDiskIO;

        DiskIoThreadExecutor() {
            mDiskIO = new ThreadPoolExecutor(1, 100, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(256), new NamedThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mDiskIO.execute(command);
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "aabills-thread-" + mThreadNum.getAndIncrement());
            return t;
        }
    }
}

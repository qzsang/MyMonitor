package com.qzsang.mymonitor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * todo 目前需要new 大量的Pair  类，这样会增大gc的压力 待优化
 */
public class StackTraceCollectThread extends Thread {
    public static long clockMillis = 1000;//超过clockMillis 毫秒 则开始打印栈信息
    public static long delayMillis = (long) (clockMillis * 0.9f);//延迟时间  delayMillis毫秒开始采集一次栈信息
    private static int mMaxCount = 100;
    private AtomicBoolean startDump = new AtomicBoolean(false); //做标记
    private List<Pair<Long, String>> stackTraceList = new ArrayList<>(mMaxCount);

    private Handler mHandler;

    public void run() {
        Looper.prepare();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                // process incoming messages here
                if (msg != null && msg.getCallback() != null) {
                    msg.getCallback().run();
                }
            }
        };

        Looper.loop();
    }


    public List<Pair<Long, String>> getStackTraceList() {
        return stackTraceList;
    }

    //每delayMillis毫秒循环一次
    //注： clockCanry 框架是  设置的时间的0.8倍
    private Runnable mRunnable =  new Runnable() {
        @Override
        public void run() {
            StackTraceElement[] stackTraceElements = Looper.getMainLooper().getThread().getStackTrace();
            if (stackTraceElements.length <= 0) {
                StackTraceCollectThread.this.mHandler.postDelayed(this, delayMillis);
                return;
            }

            StringBuffer stringBuffer = new StringBuffer();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                stringBuffer.append(stackTraceElement.toString() + "\r\n");
            }
            synchronized (stackTraceList) {
                if (stackTraceList.size() == mMaxCount) {
                    stackTraceList.remove(0);
                }
                stackTraceList.add(new Pair<Long, String>(System.currentTimeMillis(), stringBuffer.toString()));
            }
            StackTraceCollectThread.this.mHandler.postDelayed(this, delayMillis);
        }
    };

    public void startDump() {
        if (startDump.get()) {
            return;
        }
        startDump.set(true);
        if (mHandler != null && mRunnable != null) {
            mHandler.postDelayed(mRunnable, delayMillis);
        }
    }
    public void stopDump () {
        if (!startDump.get()) {
            return;
        }
        startDump.set(false);

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

}

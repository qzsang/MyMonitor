package com.qzsang.mymonitor;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    StackTraceCollectThread stackTraceCollectThread = new StackTraceCollectThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void click (View view) {
        execute();
    }

    public void execute (){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        stackTraceCollectThread.start();

        // TODO: 2017/8/29 进行封装
        Looper.getMainLooper().setMessageLogging(new Printer() {
            private boolean mPrintingStarted = false;
            private long mStartTimestamp = 0;

            @Override
            public void println(String x) {

                if (!mPrintingStarted) {
                    //将  StackTraceCollectThread  数据采集开启
                    stackTraceCollectThread.startDump();

                    mStartTimestamp = System.currentTimeMillis();
                    mPrintingStarted = true;
                } else {
                    // 将  StackTraceCollectThread  数据采集关闭
                    stackTraceCollectThread.stopDump();

                    final long endTime = System.currentTimeMillis();
                    mPrintingStarted = false;
                    //设置超过 StackTraceCollectThread.delayMillis 毫秒时打印日志
                    if (endTime - mStartTimestamp > StackTraceCollectThread.clockMillis) {

                        Log.e("Looper", "界面卡了"  + (endTime - mStartTimestamp ) + "ms,以下是对应时间点的栈信息");
                        List<Pair<Long, String>> stackTraceList = stackTraceCollectThread.getStackTraceList();
                        int count = 0;
                        for (Pair<Long, String> stackTrace : stackTraceList) {
                            if (mStartTimestamp < stackTrace.first && endTime > stackTrace.first) {
                                count++;
                                Log.e("Looper-" + (count * StackTraceCollectThread.delayMillis ) + "ms" , stackTrace.second + "");
                            }
                        }
                    }

                }

            }
        });

    }
}

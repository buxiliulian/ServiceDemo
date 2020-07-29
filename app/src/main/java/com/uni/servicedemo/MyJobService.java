package com.uni.servicedemo;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.uni.servicedemo.util.LogUtil;
import com.uni.servicedemo.util.StorageUtil;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyJobService extends JobService {

    private OkHttpClient mOkHttpClient;

    private class JobServiceHandler extends Handler {
        public JobServiceHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MainActivity.JOB_ID_DOWNLOAD_MEIZI:
                    JobParameters parameters = (JobParameters) msg.obj;
                    final PersistableBundle bundle = parameters.getExtras();
                    String url = bundle.getString(MainActivity.JOBINFO_EXTRA_KEY_URL);
                    String name = bundle.getString(MainActivity.JOBINFO_EXTRA_KEY_FILE_NAME);
                    if ( null == url || null == name) {
                        return;
                    }
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    try {
                        final Response response = mOkHttpClient.newCall(request).execute();
                        if (response.isSuccessful()) {
                            // 保存下载的图片
                            final ResponseBody body = response.body();
                            if (body != null) {
                                if (StorageUtil.saveImage(name, body.bytes())) {
                                    // 发送广播通知Activity显示图片
                                    Intent intent = new Intent(MainActivity.RECEIVER_ACTION);
                                    intent.setPackage(getPackageName());
                                    intent.putExtra(MainActivity.JOBINFO_EXTRA_KEY_FILE_NAME, name);
                                    sendBroadcast(intent);
                                    // 通知系统任务已经完成
                                    jobFinished(parameters, false);
                                    LogUtil.d("Job[" + msg.what + "] finished.");
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private JobServiceHandler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("JobService_thread");
        handlerThread.start();
        mHandler = new JobServiceHandler(handlerThread.getLooper());
        mOkHttpClient = new OkHttpClient();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        LogUtil.d("onStartJob");
        // 把任务交给工作线程处理
        final Message message = mHandler.obtainMessage();
        message.what = params.getJobId();
        message.obj = params;
        message.sendToTarget();
        // 返回true表明任务在工作线程中继续执行
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogUtil.d("onStopJob");
        // 下载图片需要网络，如果网络断开，下载任务会自动发生IO异常，任务就自动中断，因此这里不需要手动停止任务

        // 返回false表示任务不需要再次调度执行
        return false;
    }
}

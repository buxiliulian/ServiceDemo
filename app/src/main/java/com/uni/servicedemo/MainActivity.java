package com.uni.servicedemo;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.permissionx.guolindev.PermissionX;
import com.uni.servicedemo.util.StorageUtil;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    public static final int JOB_ID_DOWNLOAD_MEIZI = 1;
    private static final String IMAGE_URL = "https://ae01.alicdn.com/kf/Uec00959acd9c4d0aa900d5fb8ea481931.jpg";
    private static final String IMAGE_NAME = "meizi.jpeg";
    public static final String RECEIVER_ACTION = "com.uni.servicedemo.result";
    public static final String JOBINFO_EXTRA_KEY_URL = "url";
    public static final String JOBINFO_EXTRA_KEY_FILE_NAME = "fileName";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String fileName = intent.getStringExtra(JOBINFO_EXTRA_KEY_FILE_NAME);
            final Bitmap image = StorageUtil.getImage(fileName);
            mImageView.setImageBitmap(image);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.image_view);

        registerReceiver(mReceiver, new IntentFilter(RECEIVER_ACTION));

        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID_DOWNLOAD_MEIZI, new ComponentName(this, MyJobService.class))
                                .setExtras(createExtras())
                                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                        jobScheduler.schedule(builder.build());
                    }
                });

    }

    @NotNull
    private PersistableBundle createExtras() {
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putString(JOBINFO_EXTRA_KEY_URL, IMAGE_URL);
        persistableBundle.putString(JOBINFO_EXTRA_KEY_FILE_NAME, IMAGE_NAME);
        return persistableBundle;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
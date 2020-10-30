package com.jeevan.whatsapp.WorkManagerHandler;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jeevan.whatsapp.Activities.SettingActivity;

public class MainWorkManager extends Worker {

    private static final String TAG = MainWorkManager.class.getSimpleName();

    public MainWorkManager(@NonNull Context context,
                           @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        return Result.success();
    }
}

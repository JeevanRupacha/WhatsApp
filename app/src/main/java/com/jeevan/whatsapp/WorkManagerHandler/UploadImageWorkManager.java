package com.jeevan.whatsapp.WorkManagerHandler;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jeevan.whatsapp.Activities.SettingActivity;

public class UploadImageWorkManager extends Worker {

    private static final String TAG = UploadImageWorkManager.class.getSimpleName();

    private SettingActivity settingActivity = new SettingActivity();


    public UploadImageWorkManager(@NonNull Context context,
                                  @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

//        new SettingActivity().uploadProfileImage();

        return Result.success();
    }


}
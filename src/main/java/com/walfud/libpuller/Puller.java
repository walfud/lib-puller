package com.walfud.libpuller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by walfud on 10/12/15.
 */
public class Puller {

    public static final String TAG = "Puller";
    private static final int REQUEST_PULLER = 0x1;
    private static final int ID_PULLER = 0x1000;
    private static Puller sInstance;

    private Context mContext;

    public static Puller getInstance() {
        if (sInstance == null) {
            sInstance = new Puller();
        }

        return sInstance;
    }

    public void initialize(Context context) {
        mContext = context;

        createReceiver();
        createNotification();
    }

    private void createReceiver () {
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final File srcDir = mContext.getFilesDir().getParentFile();
                        final File dstParentDir = Environment.getExternalStorageDirectory();
                        final File dstDir = new File(dstParentDir, mContext.getPackageName() + "." + Calendar.getInstance().getTimeInMillis());

                        new AsyncTask<Void, Void, Void>() {
                            private List<File> mSuccessList = new ArrayList<>();
                            private List<File> mFailList = new ArrayList<>();

                            @Override
                            protected Void doInBackground(Void... params) {

                                try {
                                    if (dstParentDir.canWrite() && dstDir.mkdirs()) {

                                        for (File file : srcDir.listFiles()) {

                                            Process process = Runtime.getRuntime().exec(String.format("cp -Rf %s %s", file.getAbsolutePath(), dstDir.getAbsolutePath()));
                                            process.waitFor();

                                            (process.exitValue() == 0 ? mSuccessList : mFailList).add(file);
                                        }
                                    } else {
                                        mFailList = Arrays.asList(srcDir.listFiles());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void vVoid) {
                                super.onPostExecute(vVoid);

                                int totalCount = srcDir.listFiles().length;
                                int successCount = mSuccessList.size();
                                int failCount = mFailList.size();

                                if (successCount == totalCount) {
                                    // All success
                                    Toast.makeText(mContext, String.format("Success(%d/%d)", successCount, totalCount), Toast.LENGTH_SHORT).show();
                                } else {
                                    // Something wrong
                                    StringBuilder failNamesBuilder = new StringBuilder();
                                    for (File file : mFailList) {
                                        failNamesBuilder.append("\n---" + file.getName());
                                    }

                                    Toast.makeText(mContext, String.format("Fail(%d/%d)%s", successCount, totalCount, failNamesBuilder.toString()), Toast.LENGTH_LONG).show();
                                }
                            }
                        }.execute();
                    }
                }, new IntentFilter(getActionString()));
    }

    private void createNotification() {
        Intent intent = new Intent(getActionString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, REQUEST_PULLER, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(mContext)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setContentTitle(mContext.getPackageName())
                .setContentText(Environment.getExternalStorageDirectory().getAbsolutePath())
                .setContentInfo(Environment.getExternalStorageState())
                .setSmallIcon(R.drawable.ic_sd_storage_white_48dp)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_PULLER, notification);
    }

    private String getActionString() {
        return String.format("%s.ACTION.PULLER", mContext.getPackageName());
    }
}

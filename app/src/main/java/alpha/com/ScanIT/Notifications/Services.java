package alpha.com.ScanIT.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import alpha.com.ScanIT.MainActivity;
import alpha.com.ScanIT.R;
import alpha.com.ScanIT.databases.TinyDB;
import static android.graphics.Color.rgb;


public class Services extends Service {
    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    Boolean Notification_SHOWN;
    Boolean DEBUG_TRUE = false;
    int Your_X_SECS = 1;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG_TRUE) {
            Log.e(TAG, "onStartCommand");
        }
        super.onStartCommand(intent, flags, startId);
        Notification_SHOWN = false;
        startTimer();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (DEBUG_TRUE) {
            Log.e(TAG, "onCreate");
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG_TRUE) {
            Log.e(TAG, "onDestroy");
        }
        stoptimertask();
        super.onDestroy();
    }
    final Handler handler = new Handler();

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            if (DEBUG_TRUE) {
                Log.e(TAG, "Stopped.");
            }
        }
    }

    public void showNotification() {

        final TinyDB tinydb = new TinyDB(this);
        String subText;
        Integer ID = 001;
        int Packages = tinydb.getInt("counter", 0);
        if (Packages != 0) {
            if (Packages == 1) {
                subText = " package";
            } else {
                subText = " packages";
            }

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setPriority(2)
                            .setVibrate(new long[0])
                            .setSmallIcon(R.drawable.ic_dialog_alert_holo_light)
                            .setLargeIcon(BitmapFactory.decodeResource( getResources(), R.drawable.ic_info))
                            .setContentTitle("You have scanned" + " " + Packages + subText + "")
                            .setContentText("Touch to scan more.")
                            .setColor(rgb(105, 105, 105))
                            .setAutoCancel(true)
                            .setVisibility(1);

            Intent resultIntent = new Intent(this, Services.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            mBuilder.setContentIntent(pIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(ID, mBuilder.build());

        }
    }

    public void initializeTimerTask() {
        final TinyDB tinydb = new TinyDB(this);
        final int Packages = tinydb.getInt("counter", 0);

        timerTask = new TimerTask() {
            public void run() {

                handler.post(new Runnable() {
                    public void run() {

                        if (!Notification_SHOWN && Packages != 0) {
                            showNotification();
                            Notification_SHOWN = true;
                            stoptimertask();
                            if (DEBUG_TRUE) {
                                Log.e(TAG, String.valueOf(Packages));
                                Log.e(TAG, "Showing..");
                            }
                        }
                        else
                        {
                            stoptimertask();
                            if (DEBUG_TRUE) {
                                Log.e(TAG, String.valueOf(Packages));
                                Log.e(TAG, "Didn't show.");
                            }
                        }
                    }
                });
            }
        };
    }
}
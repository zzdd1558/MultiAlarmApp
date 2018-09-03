package alarm.project.com.alarmapp.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import alarm.project.com.alarmapp.Alarm_Start_Activity;
import alarm.project.com.alarmapp.MainActivity;
import alarm.project.com.alarmapp.R;

public class AlarmReceiver extends BroadcastReceiver {

    private PowerManager.WakeLock wakeLock;
    @Override
    public void onReceive(Context context, Intent intent) {

        // 화면 깨운후 알람 Activity로 이동.
        Toast.makeText(context, "Alarm Received!", Toast.LENGTH_LONG).show();
        Log.i("TAGG RECEIVER" , "HELLO RECEIVER");
        Log.i("TAGG RECEIVER" , intent.getIntExtra("requestCode" , -1) + "");

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "hi");

        wakeLock.acquire();


        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        Intent it = new Intent(context , Alarm_Start_Activity.class);
        it.putExtra("requestCode" , intent.getIntExtra("requestCode" , -1));
        PendingIntent pi = PendingIntent.getActivity(context , -1 , it , PendingIntent.FLAG_ONE_SHOT);
        try{
            pi.send();
        }catch (PendingIntent.CanceledException e ) {
            e.printStackTrace();
        }
    }
}

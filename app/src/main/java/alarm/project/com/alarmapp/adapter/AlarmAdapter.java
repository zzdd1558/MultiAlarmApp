package alarm.project.com.alarmapp.adapter;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncContext;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import alarm.project.com.alarmapp.MainActivity;
import alarm.project.com.alarmapp.R;
import alarm.project.com.alarmapp.SetAlarm;
import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;
import alarm.project.com.alarmapp.receiver.AlarmReceiver;
import alarm.project.com.alarmapp.utils.TimeSplitUtils;

public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "AlarmAdapter";

    List<AlarmRecordDTO> recordList = null;

    public void AlarmAdapter() {

    }

    public AlarmAdapter(List<AlarmRecordDTO> list) {
        this.recordList = list;
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener , View.OnLongClickListener{

        private final String TAG = "AlarmViewHolder";
        private int alarmRequestCode;

        private LinearLayout alarmLayout = null;
        private TextView alarmAmPm = null;
        private TextView alarmTime = null;
        private TextView alarmDay = null;
        private Switch alarmYN = null;

        private final Context context;

        private DatabaseHelper db = null;


        public AlarmViewHolder(View itemView) {
            super(itemView);

            context = itemView.getContext();

            db = new DatabaseHelper(context);

            alarmLayout = itemView.findViewById(R.id.alarm_layout);
            alarmAmPm = itemView.findViewById(R.id.alarm_am_pm);
            alarmTime = itemView.findViewById(R.id.alarm_time);
            alarmDay = itemView.findViewById(R.id.alarm_day);
            alarmYN = itemView.findViewById(R.id.alarm_yn);

            alarmLayout.setOnClickListener(this);
            alarmLayout.setOnLongClickListener(this);
            alarmYN.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.alarm_layout :
                    Log.i(TAG, "Hello :: " + alarmRequestCode);
                    Intent it = new Intent( context, SetAlarm.class);
                    it.putExtra("method" , "update");
                    it.putExtra("requestCode" , alarmRequestCode);
                    context.startActivity(it);

                    break;
                case R.id.alarm_yn :
                    Log.i(TAG , alarmYN.isChecked() + "" + alarmRequestCode);

                    String flag = alarmYN.isChecked() == true ? "Y" : "N";
                    db.soundCheck( flag, alarmRequestCode);
                    AlarmRecordDTO record = db.onSelectOne(alarmRequestCode);

                    Log.i(TAG , record.getRegistTime());

                    int[] setCalTime = TimeSplitUtils.calTimeSplit(record.getRegistTime());

                    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();

                    calendar.set(setCalTime[0] , setCalTime[1] - 1 , setCalTime[2] , setCalTime[3] , setCalTime[4]);
                    calendar.set(Calendar.SECOND , 0);

                    Log.i(TAG , calendar.getTimeInMillis() + "");
                    Log.i(TAG , Calendar.getInstance().getTimeInMillis() + "");


                    Intent intent = new Intent(context , AlarmReceiver.class);
                    PendingIntent operation = PendingIntent.getBroadcast(context , alarmRequestCode , intent , PendingIntent.FLAG_UPDATE_CURRENT);

                    if ("Y".equals(flag)){
                        if (calendar.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()){
                            if (Build.VERSION.SDK_INT >= 23){
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP , calendar.getTimeInMillis() , operation);
                            }else{
                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                    alarmManager.set(AlarmManager.RTC_WAKEUP , calendar.getTimeInMillis() , operation );
                                } else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP , calendar.getTimeInMillis() , operation );
                                }
                            }
                        }

                    }else{
                        if ( operation != null ){
                            alarmManager.cancel(operation);
                            operation.cancel();
                        }
                    }


                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {

            switch (v.getId()){
                case R.id.alarm_layout :

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                    alertDialogBuilder.setTitle("알람");
                    alertDialogBuilder
                            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                public boolean onKey(DialogInterface dialog,
                                                     int keyCode, KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                                        //dialog.dismiss();
                                        return true;
                                    }
                                    return false;
                                }
                            });
                    // AlertDialog 셋팅
                    alertDialogBuilder
                            .setMessage("해당 알람을 삭제합니다.")
                            .setCancelable(false)
                            .setPositiveButton("삭제",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            // 프로그램을 종료한다
                                            Log.i(TAG , "Long Click ::" + alarmRequestCode);
                                            db.onDeleteOne(alarmRequestCode);

                                            MainActivity.RefreshView.recyclerViewRefresh(db.selectAll());

                                            Snackbar.make(itemView , "알람이 삭제 되었습니다." , Snackbar.LENGTH_SHORT).show();

                                            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                                            Intent intent = new Intent(context , AlarmReceiver.class);
                                            PendingIntent operation = PendingIntent.getBroadcast(context, alarmRequestCode , intent , PendingIntent.FLAG_UPDATE_CURRENT);
                                            if ( operation != null ){
                                                alarmManager.cancel(operation);
                                                operation.cancel();
                                            }
                                        }
                                    })
                            .setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // 다이얼로그 생성
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // 다이얼로그 보여주기
                    alertDialog.show();
                    break;
            }

            return true;
        }
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recylcer_view_row, parent, false);

        return new AlarmViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AlarmViewHolder alarmViewHolder = (AlarmViewHolder) holder;

        String holderTime = recordList.get(position).getRegistTime();
        String[] splitTime = holderTime.split("-");

        String showAmOrPm = "0".equals(splitTime[0]) ? "AM" : "PM";
        String showTime = splitTime[1];
        String showDate = splitTime[2];


        alarmViewHolder.alarmRequestCode = recordList.get(position).getRequestCode();
        alarmViewHolder.alarmAmPm.setText(showAmOrPm);
        alarmViewHolder.alarmTime.setText(showTime + "분");
        alarmViewHolder.alarmDay.setText(showDate);
        alarmViewHolder.alarmYN.setChecked("Y".equals(recordList.get(position).getAlarmFlag()) ? true : false);


    }

    @Override
    public int getItemCount() {
        Log.i(TAG, recordList.size() + "");
        return recordList.size();
    }
}

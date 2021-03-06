package alarm.project.com.alarmapp.adapter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncContext;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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

/* RecyclerView 연결을 위한 Adapter */
public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* Log Name */
    private final String TAG = "AlarmAdapter";

    /* AlarmList를 담을 변수 */
    List<AlarmRecordDTO> recordList = null;

    /* 기본 생성자 */
    public void AlarmAdapter() {
    }

    /* 오버로딩 생성자 AlarmList를 매개변수로 전달 받음. */
    public AlarmAdapter(List<AlarmRecordDTO> list) {
        this.recordList = list;
    }


    /*
     * ViewHolder Inner static Class
     *
     * 일반 Click과 LongClick클래스 implements
     *
     * */
    public static class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        /* Log Name */
        private final String TAG = "AlarmViewHolder";

        /* Alarm을 구분하기 위한 고유 값 변수 */
        private int alarmRequestCode;

        /* List에 보여질 변수들 초기화. */
        private LinearLayout alarmLayout = null;
        private TextView alarmAmPm = null;
        private TextView alarmTime = null;
        private TextView alarmDay = null;
        private Switch alarmYN = null;

        /* context */
        private final Context context;

        /* db 변수 */
        private DatabaseHelper db = null;


        public AlarmViewHolder(View itemView) {
            super(itemView);

            // itemView로 부터 Context를 받아 사용 가능.
            context = itemView.getContext();

            // DB 사용을 위한 초기화
            db = new DatabaseHelper(context);

            // init Component
            alarmLayout = itemView.findViewById(R.id.alarm_layout);
            alarmAmPm = itemView.findViewById(R.id.alarm_am_pm);
            alarmTime = itemView.findViewById(R.id.alarm_time);
            alarmDay = itemView.findViewById(R.id.alarm_day);
            alarmYN = itemView.findViewById(R.id.alarm_yn);

            // Event 등록
            alarmLayout.setOnClickListener(this);
            alarmLayout.setOnLongClickListener(this);
            alarmYN.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                /* 알람 Layout을 클릭하열을 경우 . Animation을 활용하면서 수정 하는 Activity로 이동.*/
                case R.id.alarm_layout:
                    //Log.i(TAG, "Hello :: " + alarmRequestCode);

                    // 알람 수정을 위한 Event
                    Intent it = new Intent(context, SetAlarm.class);
                    it.putExtra("method", "update");
                    it.putExtra("requestCode", alarmRequestCode);
                    context.startActivity(it);

                    // Animation 처리 .
                    ((AppCompatActivity) context).overridePendingTransition(R.anim.slide_up, R.anim.stay);

                    break;

                /*  알람을 울릴지 말지 에 대한 Y/N 체크 버튼  */
                case R.id.alarm_yn:

                    // DB에 저장되어 있는 Flag 수정을 위한 삼항 연산자 조건.
                    String flag = alarmYN.isChecked() == true ? "Y" : "N";

                    /* alarmRequestCode를 조건문으로 사용하기 위해 Flag와 함께 전달., */
                    db.soundCheck(flag, alarmRequestCode);

                    /* alarmRequestCode로 해당되는 Alarm에 대한 값들을 가져옴. */
                    AlarmRecordDTO record = db.onSelectOne(alarmRequestCode);

                    /* 알람 재설정을 위한 시간 재설정. */
                    int[] setCalTime = TimeSplitUtils.calTimeSplit(record.getRegistTime());

                    /* 알람 서비스를 위한 Manager 등록. */
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();

                    /* N에서 Y로  바뀌었을시 알람등록을 위한 시간 settings */
                    calendar.set(setCalTime[0], setCalTime[1] - 1, setCalTime[2], setCalTime[3], setCalTime[4]);
                    calendar.set(Calendar.SECOND, 0);

                    /* 추후에 발생될 이벤트를 담을 Intent와 PendingIntent 등록. */
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    PendingIntent operation = PendingIntent.getBroadcast(context, alarmRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // flag 가 Y일 경우 알람을 울린다. N 일경우는 알람등록을 하지 않고 해당 알람을 취소한다.
                    if ("Y".equals(flag)) {
                        if (calendar.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
                            } else {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
                                } else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
                                }
                            }
                        }
                    } else {
                        if (operation != null) {
                            alarmManager.cancel(operation);
                            operation.cancel();
                        }
                    }
                    break;
            }
        }


        // Long클릭할 경우 알람 삭제.
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.alarm_layout:

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
                                            Log.i(TAG, "Long Click ::" + alarmRequestCode);
                                            db.onDeleteOne(alarmRequestCode);

                                            /* 삭제한후 그 액티비티에 있는 RecyclerView Refresh */
                                            MainActivity.RefreshView.recyclerViewRefresh(db.selectAll());

                                            /* 해당 requestCode로 등ㅇ록되있는 알람 삭제. */
                                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                            Intent intent = new Intent(context, AlarmReceiver.class);
                                            PendingIntent operation = PendingIntent.getBroadcast(context, alarmRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                            if (operation != null) {
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


    // Recycler ViewHolder 생성 부분.

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recylcer_view_row, parent, false);
        return new AlarmViewHolder(v);
    }


    /**
     * 처음 RecyclerView에 그려질때 설정되는 텍스트 및 체크
     *  직접적으로 리스트에 값을 settings 해주는 부분.
     */
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

    // RecyclerView에 뿌려지는 Item의 갯수.
    @Override
    public int getItemCount() {
        Log.i(TAG, recordList.size() + "");
        return recordList.size();
    }
}

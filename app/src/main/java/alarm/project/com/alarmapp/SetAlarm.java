package alarm.project.com.alarmapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;
import alarm.project.com.alarmapp.models.AlarmVO;
import alarm.project.com.alarmapp.receiver.AlarmReceiver;
import alarm.project.com.alarmapp.utils.SharedPrefsUtils;
import alarm.project.com.alarmapp.utils.TimeSplitUtils;

public class SetAlarm extends AppCompatActivity implements View.OnClickListener {

    // Context
    private Context                 mCtx = null;

    // 시간 설정 관련 변수
    private NumberPicker            am_pm = null;
    private NumberPicker            hour = null;
    private NumberPicker            minute = null;

    // Calendar 및 Date 관련 변수.
    private Calendar                calendar = null;
    private Date                    date = null;

    // Sound 설정 관련 SeekBar 변수.
    private SeekBar                 soundController = null;

    // 날짜 선택 관련 버튼
    private Button                  selectDate = null;

    // 알람 저장 or 취소 관련 버튼
    private Button                  cancelBtn = null;
    private Button                  registBtn = null;

    // 알람 시간 관련 VO
    private AlarmVO                 alarmVO = null;

    // SimpleDateFormat Pattern
    private String                  setPattern = "yyyy년 M월 dd일 (E)";

    // Log Label
    private static final String     TAG = "SetAlarm";

    // DB Helper
    private DatabaseHelper          db = null;

    // AudioManager
    private AudioManager            audioManager = null;

    // Alarm데이터 사용 관련 VO
    private AlarmRecordDTO          record = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        // Component 초기화.
        initComponents();

        // method가 insert or update 값에 따라 추가 or 수정을 구분 하기 위한 intent 값.
        String method = getIntent().getStringExtra("method").toString();

        //event 등록.
        selectDate.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        registBtn.setOnClickListener(this);

        // SimpleDateFormat 설정 .
        String dateString = String2NumberDate(setPattern);

        // 오늘 날짜로 TextView 변경.
        ((TextView) findViewById(R.id.today)).setText(dateString);


        // am_pm 관련 Number Picker 세팅.
        // 오전 : 0  , 오후 : 1
        am_pm.setMinValue(0);
        am_pm.setMaxValue(1);
        am_pm.setValue(String2NumberDate("a").equals("오전") ? 0 : 1);
        am_pm.setDisplayedValues(new String[]{
                "오전", "오후"
        });

        // NumberPicker에 focus disabled
        am_pm.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // hour 관련 Number Picker 세팅.
        hour.setMinValue(0);
        hour.setMaxValue(23);

        // 한바퀴 다돌면 처음으로 .
        hour.setWrapSelectorWheel(true);
        hour.setValue(alarmVO.getHour());

        // NumberPicker에 focus disabled
        hour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // minute 관련 Number Picker 세팅.
        minute.setMinValue(0);
        minute.setMaxValue(59);

        // 한바퀴 다돌면 처음으로 .
        minute.setWrapSelectorWheel(true);
        minute.setValue(alarmVO.getMinute());

        // NumberPicker에 focus disabled
        minute.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // Audio Manager 사운드 컨트롤
        // audioManager의 max값으 가져옴.
        int nMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // 현재 기기의 볼륨을 가져옴.
        int nCurrentVolumn = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundController.setMax(nMax);
        soundController.setProgress(nCurrentVolumn);
        record.setAlarmSound(nCurrentVolumn);
        soundController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                record.setAlarmSound(progress);
            }
        });

        // 수정할경우.
        if ("update".equals(method)){
            int requestCode = getIntent().getIntExtra("requestCode" , -1);
            if(requestCode == -1){
                Toast.makeText(mCtx , "잘못된 접근입니다." , Toast.LENGTH_SHORT ).show();
            }else{
                AlarmRecordDTO data = db.onSelectOne(requestCode);
                String[] timeSplit = TimeSplitUtils.timeSplit(data.getRegistTime() , "-");
                String[] hour_minute_split = TimeSplitUtils.timeSplit(timeSplit[1],":");
                String[] month_split = TimeSplitUtils.timeSplit(timeSplit[2] , "월");
                String[] day_split = TimeSplitUtils.timeSplit(month_split[1] , "일");

                // 년 월 일 시간 분 필요.
                int mYear = Integer.parseInt(timeSplit[3]);
                int mMonth = Integer.parseInt(month_split[0]);
                int mDayOfMonth = Integer.parseInt(day_split[0].trim());
                int mHour = Integer.parseInt(hour_minute_split[0]);
                int mMinute = Integer.parseInt(hour_minute_split[1]);

                alarmVO.setYear(mYear);
                alarmVO.setMonth(mMonth);
                alarmVO.setDayOfMonth(mDayOfMonth);

                am_pm.setValue(Integer.parseInt(timeSplit[0]));

                hour.setValue(mHour);
                minute.setValue(mMinute);

                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC , data.getAlarmSound() , 0);
                record.setAlarmSound(data.getAlarmSound());
                soundController.setProgress(data.getAlarmSound());

                ((TextView) findViewById(R.id.today)).setText(mYear + "년 " + timeSplit[2]);
            }
        }
    }

    // 알람 등록부분.
    public void registAlarm(Context context, AlarmVO alarm , String method) {

        int requestCode = 0;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SetAlarm.this , AlarmReceiver.class);

        if("update".equals(method)){
            requestCode = getIntent().getIntExtra("requestCode" , -1);
        }else{
            requestCode = SharedPrefsUtils.getIntegerPreference(mCtx , getString(R.string.reqeust_code) , 0);
            intent.putExtra("requestCode" , requestCode);
            requestCode++;
            SharedPrefsUtils.setIntegerPreference(mCtx , getString(R.string.reqeust_code) , requestCode);
        }

        intent.putExtra("requestCode" , requestCode);

        PendingIntent operation = PendingIntent.getBroadcast(SetAlarm.this , requestCode , intent , PendingIntent.FLAG_UPDATE_CURRENT);

        // 이미 등록되어있는 Alarm일 경우 취소후 다시 등록.
        if ( operation != null ){
            alarmManager.cancel(operation);
            operation.cancel();
            operation = PendingIntent.getBroadcast(SetAlarm.this , requestCode , intent , PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Calendar calendar = Calendar.getInstance();
        // Month는 0 ~ 11 이기 때문에 -1을 해줌
        calendar.set(alarm.getYear() , alarm.getMonth() -1 , alarm.getDayOfMonth() , alarm.getHour() , alarm.getMinute());
        calendar.set(Calendar.SECOND , 0);

        StringBuilder builder = new StringBuilder();
        builder.append(am_pm.getValue());
        builder.append("-");
        builder.append(alarm.getHour() + ":" + (alarm.getMinute() < 10 ? "0" + alarmVO.getMinute() : alarm.getMinute()));
        builder.append("-");
        builder.append(alarm.getMonth() + "월 " + alarm.getDayOfMonth() + "일");
        builder.append(" (" +  updateDate(alarm.getYear() , alarm.getMonth() -1 , alarm.getDayOfMonth() , false) + ")");
        builder.append("-");
        builder.append(alarm.getYear());

        record.setRequestCode(requestCode);
        record.setRegistTime(builder.toString());
        record.setAlarmFlag("Y");

        // update or insert
        if("update".equals(method)){
            db.onUpdate(record);
        }else{
            db.onInsert(record);
        }


        // 버전별 알람 등록이 다르기 때문에 분기처리.
        if( "Y".equals(record.getAlarmFlag())){
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
        cancelFunc();
    }

    public String updateDate(int year, int month, int day , boolean check) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);

        // date의 값이기때문에 추가로 +1해서 넘겨서 다시 -1 해준다.
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, day);

        String dayOfTheWeek = null;
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                dayOfTheWeek = "일";
                break;
            case 2:
                dayOfTheWeek = "월";
                break;
            case 3:
                dayOfTheWeek = "화";
                break;
            case 4:
                dayOfTheWeek = "수";
                break;
            case 5:
                dayOfTheWeek = "목";
                break;
            case 6:
                dayOfTheWeek = "금";
                break;
            case 7:
                dayOfTheWeek = "토";
                break;
        }
        if (!check) {
            return dayOfTheWeek;
        }
        ((TextView) findViewById(R.id.today)).setText(year + "년 " + (month + 1) + "월 " + day + "일 (" + dayOfTheWeek + ")");
        return "";
    }

    // 취소 버튼 override 재설정.
    @Override
    public void onBackPressed() {
        showDialog("알람 설정", "알람 설정을 취소하고 뒤로가시겠습니까?");
    }

    // 취소할 경우 finish() 후 Animation 추가.
    public void cancelFunc() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);

    }

    // Component 초기화.
    public void initComponents() {

        //Context
        mCtx = this;

        // Number Picker
        am_pm = (NumberPicker) findViewById(R.id.am_pm);
        hour = (NumberPicker) findViewById(R.id.hour);
        minute = (NumberPicker) findViewById(R.id.minute);

        calendar = Calendar.getInstance();
        date = calendar.getTime();

        // 볼륨 조절
        soundController = (SeekBar) findViewById(R.id.sound_controller);

        // 날짜 선택
        selectDate = (Button) findViewById(R.id.select_date);

        //취소 , 설정
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        registBtn = (Button) findViewById(R.id.regist_btn);

        //db helper
        db = new DatabaseHelper(mCtx);

        //sound  manager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        record = new AlarmRecordDTO();

        // Alarm time VO
        int year = Integer.parseInt(String2NumberDate("yyyy"));
        int month = Integer.parseInt(String2NumberDate("M"));
        int day = Integer.parseInt(String2NumberDate("d"));
        int hour = Integer.parseInt(String2NumberDate("H"));
        int minute = Integer.parseInt(String2NumberDate("m"));
        alarmVO = new AlarmVO(year , month , day,  hour , minute);
    }

    // 클릭 이벤트.
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.select_date:
                DatePickerDialog dialog = new DatePickerDialog(this, listener, Calendar.YEAR, Calendar.MONTH - 1, Calendar.DAY_OF_MONTH);
                dialog.getDatePicker().setMinDate(date.getTime());
                dialog.show();
                break;

            case R.id.cancel_btn:

                showDialog("알람 설정", "알람 설정을 취소하고 뒤로가시겠습니까?");
                break;

            case R.id.regist_btn:
                alarmVO.setHour(hour.getValue());
                alarmVO.setMinute(minute.getValue());

                Log.i("TAGGG : vo" , alarmVO.toString());
                registAlarm(mCtx , alarmVO , getIntent().getStringExtra("method").toString());

                break;
        }
    }

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            updateDate(year, monthOfYear, dayOfMonth, true);
            alarmVO.setYear(year);
            alarmVO.setMonth(monthOfYear + 1);
            alarmVO.setDayOfMonth(dayOfMonth);

            Toast.makeText(getApplicationContext(), year + "년" + (monthOfYear + 1) + "월" + dayOfMonth + "일", Toast.LENGTH_SHORT).show();

        }

    };

    public String String2NumberDate(String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public void showDialog(String title, String content) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mCtx);

        alertDialogBuilder.setTitle(title);
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
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 프로그램을 종료한다
                                cancelFunc();
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

    }
}

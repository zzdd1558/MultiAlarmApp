package alarm.project.com.alarmapp;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;
import alarm.project.com.alarmapp.utils.TimeSplitUtils;

public class Alarm_Start_Activity extends Activity implements View.OnClickListener {

    private final String TAG = "Alarm_Start";

    private Context mCtx = null;
    private SpeechRecognizerClient client = null;

    private Vibrator vibe = null;

    private AudioManager audioManager = null;

    private Uri alert = null;
    private MediaPlayer player = null;
    private int alarmRequestCode;

    private DatabaseHelper db = null;

    private TextView mYearMonthDay = null;
    private TextView mHourMinute = null;
    private Button mNewton = null;
    private Button mWeather = null;

    private Handler uiHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mNewton.setEnabled(false);
            mWeather.setEnabled(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm__start_);


        initComponents();

        AlarmRecordDTO record = db.onSelectOne(alarmRequestCode);

        // 년 월 일 시 분
        int[] currentAlarm = TimeSplitUtils.calTimeSplit(record.getRegistTime());

        // BroadCast PendingIntent시 화면을 켜주기 위한 설정. 및 잠금화면 위에서 Activity 실행.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        startRington();
        startUsingSpeechSDK();
        startVibrator();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newtone:
                client.startRecording(true);
                break;
            case R.id.weather:
                break;

        }
    }

    private void initComponents() {

        mCtx = this;

        //db helper
        db = new DatabaseHelper(mCtx);

        mYearMonthDay = (TextView) findViewById(R.id.show_day);
        mHourMinute = (TextView) findViewById(R.id.show_time);
        ;
        mNewton = (Button) findViewById(R.id.newtone);
        mWeather = (Button) findViewById(R.id.weather);

        mNewton.setOnClickListener(this);
        mWeather.setOnClickListener(this);

        alarmRequestCode = getIntent().getIntExtra("requestCode", -1);

    }

    public void startRington() {
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        player = new MediaPlayer();

        try {
            player.setDataSource(this, alert);
        } catch (Exception e) {
            e.printStackTrace();
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {

            player.setAudioStreamType(AudioManager.STREAM_ALARM);

            player.setLooping(true);
            try {

                player.prepare();

            } catch (IllegalStateException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }

            player.start();
        }
    }

    public void startVibrator() {
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrate_pattern = {100, 300, 100, 700, 100, 300, 100, 700};
        vibe.vibrate(vibrate_pattern, 0);
    }

    public void startUsingSpeechSDK() {
        // SDK 초기화 부분.
        SpeechRecognizerManager.getInstance().initializeLibrary(mCtx);

        // 클라이언트 생성
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WORD)
                .setUserDictionary("알람\n꺼줘\n꺼\n오프");

        client = builder.build();

        client.setSpeechRecognizeListener(new SpeechRecognizeListener() {
            @Override
            public void onReady() {

                Log.i(TAG + " onReady", "onReady");
                new Thread()
                {
                    @Override
                    public void run() {

                        Message msg = uiHandler.obtainMessage();
                        uiHandler.sendMessage(msg);
                    }
                }.start();

            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onEndOfSpeech() {
                Log.i(TAG + "EndOfSpeech", "onEndOfSpeech");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

                Log.i(TAG + " onError", errorMsg);
            }

            @Override
            public void onPartialResult(String partialResult) {
                String[] alarmValue = new String[]{"꺼", "꺼줘", "알람 꺼", "알람 꺼줘"};

                for (int i = 0; i < alarmValue.length; i++) {
                    if (partialResult.contains(alarmValue[i])) {

                        if (vibe != null) {
                            vibe.cancel();
                            vibe = null;
                        }

                        if (player != null) {
                            player.stop();
                            player.release();
                            player = null;
                        }

                        if (client != null) {
                            client.stopRecording();
                        }

                        finish();


                    }
                }


                Log.i(TAG + "Partial", partialResult);
            }

            @Override
            public void onResults(Bundle results) {
                Log.i(TAG + " onResults", "onResults");

                ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
                ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);
                Log.i(TAG + " text.size", texts.size() + "");
                Log.i(TAG + " confs.size", confs.size() + "");
                for (int i = 0; i < texts.size(); i++) {
                    Log.i(TAG + " :: text " + i, texts.get(i).toString());
                    Log.i(TAG + " :: confs " + i, confs.get(i).toString());
                }

                mNewton.setEnabled(true);
                mWeather.setEnabled(true);

            }

            @Override
            public void onAudioLevel(float audioLevel) {
                Log.i(TAG + "AudioLevel", "onAudioLevel");
            }

            @Override
            public void onFinished() {
                Log.i(TAG + " onFinished", "onFinished");
            }
        });

        client.startRecording(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 종료시점
        // API를 더이상 사용하지 않을 때 finalizeLibrary() 호출.
        SpeechRecognizerManager.getInstance().finalizeLibrary();
    }


}

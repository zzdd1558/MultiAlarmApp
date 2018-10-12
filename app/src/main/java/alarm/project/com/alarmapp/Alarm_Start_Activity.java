package alarm.project.com.alarmapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;
import alarm.project.com.alarmapp.network.RetroClient;
import alarm.project.com.alarmapp.utils.TimeSplitUtils;
import alarm.project.com.alarmapp.weatherAPI.ApiService;
import alarm.project.com.alarmapp.weatherAPI.WeatherInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Alarm_Start_Activity extends Activity implements View.OnClickListener , LocationListener , TextToSpeechListener{

    private final String TAG = "Alarm_Start";

    private Context mCtx = null;
    private SpeechRecognizerClient client = null;

    private Vibrator vibe = null;


    private AudioManager audioManager = null;

    // Rintone 관련 media 재생 관련 변수
    private Uri alert = null;
    private MediaPlayer player = null;
    private int alarmRequestCode;


    // DB Helpler
    private DatabaseHelper db = null;

    // TextView,  Button Component
    private TextView mYearMonthDay = null;
    private TextView mHourMinute = null;
    private ImageButton mNewton = null;


    // 위도 경도 값
    private double dLat = 0.0;
    private double dLon = 0.0;

    // GPS사용을 위한 LocationManager.
    private LocationManager locationManager = null;

    // SK Weather API _ KEY
    private String API_KEY = null;

    // 음성 합성
    private TextToSpeechClient ttsClient;

    private NotificationManager mNotificationManager = null;

    private boolean TTSFlag = false;


    // Kakao 음성 API가 onResult를 빼곤 비동기로 움직이기때문에
    // UIHandler를 사용 하여 UI 변경.
    private Handler falseUIHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mNewton.setEnabled(false);
            mNewton.setBackgroundResource(R.drawable.alarm_btn_n);
        }
    };

    // 버튼 활성화.
    private Handler trueUIHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mNewton.setEnabled(true);
            mNewton.setBackgroundResource(R.drawable.alarm_btn);
        }
    };

    // db로부터 사용할 record
    private AlarmRecordDTO record = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm__start_);

        // BroadCast PendingIntent시 화면을 켜주기 위한 설정. 및 잠금화면 위에서 Activity 실행.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // Components 초기화.
        initComponents();


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this , Alarm_Start_Activity.class);
        resultIntent.putExtra("requestCode", getIntent().getIntExtra("requestCode", -1));
        PendingIntent alarm_close = PendingIntent.getActivity(this , 0 , resultIntent , PendingIntent.FLAG_UPDATE_CURRENT);

        //requestCode를 통해 해당 되는 record값 얻기.
        record = db.onSelectOne(alarmRequestCode);


        // 년 월 일 시 분 순으로 저장.
        int[] currentAlarm = TimeSplitUtils.calTimeSplit(record.getRegistTime());


        int mYear = currentAlarm[0];
        int mMonth = currentAlarm[1];
        int mDayOfMonth = currentAlarm[2];
        int mHour = currentAlarm[3];
        int mMinute = currentAlarm[4];


        mYearMonthDay.setText(mYear + "년 " + mMonth + "월 " + mDayOfMonth + "일 ");
        mHourMinute.setText(mHour + "시 " + mMinute + "분 ");

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC , record.getAlarmSound() , 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("알람")
                .setContentText(mHour + "시 " + mMinute + "분 ")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setOngoing(true);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(android.R.drawable.sym_action_chat , "알람 해제" , alarm_close).build();

        mBuilder.addAction(action);


        mNotificationManager.notify(alarmRequestCode , mBuilder.build());


        // 알람 bell play
        startRington();

        // 진동 시작.
        startVibrator();

        startUsingSpeechSDK();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newtone:
                // 카카오 음성 인식 API 호출.
                // 알람 시작과 동시에 명령 가능.
                startUsingSpeechSDK();
                //client.startRecording(false);
                break;
        }
    }

    private void requestLocation() {
        Log.i(TAG , "requestLocation");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mCtx, "권한체크 필요 !!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(mCtx, "GPS활성화 필요 !!", Toast.LENGTH_SHORT).show();
            checkGPS();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    private void doGeoCoding(double lat, double lon)
    {
        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> results = geocoder.getFromLocation(lat, lon, 1);
            Log.d("TAG", results.get(0).getAddressLine(0));

        } catch (Exception e) {
            e.toString();
        }
    }

    private void readMemo(){
        Handler(falseUIHandler);

        ttsClient.setSpeechText(record.getMemo());
        ttsClient.play();
    }


    private void makeWeatherMent(WeatherInfo weatherInfo)
    {
        String weatherMent="";
        weatherMent = String.format(
                getString(R.string.weather_ment_1)
                , weatherInfo.getWeather().getHourly().get(0).getGrid().getCity()
                , weatherInfo.getWeather().getHourly().get(0).getGrid().getCounty()
                , weatherInfo.getWeather().getHourly().get(0).getGrid().getVillage()
                , weatherInfo.getWeather().getHourly().get(0).getSky().getName()
                , Math.round(Float.parseFloat(weatherInfo.getWeather().getHourly().get(0).getTemperature().getTc()))+ ""
                , Math.round(Float.parseFloat(weatherInfo.getWeather().getHourly().get(0).getTemperature().getTmax()))+ ""
                , Math.round(Float.parseFloat(weatherInfo.getWeather().getHourly().get(0).getTemperature().getTmin()))+ ""
                , Math.round(Float.parseFloat(weatherInfo.getWeather().getHourly().get(0).getHumidity()))+ ""
                , weatherInfo.getWeather().getHourly().get(0).getWind().getWspd());


        Handler(falseUIHandler);

        /*if(player.isPlaying()){
            player.stop();
        }*/
        TTSFlag = true;
        ttsClient.setSpeechText(weatherMent);
        ttsClient.play();


        Log.d(TAG, "tv_weatherMent : " + weatherMent);
    }

    private void getWeatherInfo(double lat, double lon)
    {
        ApiService service = RetroClient.getApiService();
        Call<WeatherInfo> resultCall = service.getCurrentWeather(API_KEY, 2, lat, lon);

        resultCall.enqueue(new Callback<WeatherInfo>() {
            @Override
            public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {

                if(response.isSuccessful())
                {
                    Log.d("TAG", "response.body() : " + new Gson().toJson(response.body()));
                    //tv_weatherStatus.setText(new Gson().toJson(response.body()));
                    makeWeatherMent(response.body());
                }
            }

            @Override
            public void onFailure(Call<WeatherInfo> call, Throwable t) {
                Toast.makeText(mCtx, "실패", Toast.LENGTH_SHORT).show();
                Log.d("TAG", "retrofit2 error : " + t.getMessage());
            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged()");
        Handler(falseUIHandler);
        dLat = location.getLatitude();
        dLon = location.getLongitude();

        locationManager.removeUpdates(this);
        doGeoCoding(dLat, dLon);
        getWeatherInfo(dLat, dLon);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void checkGPS()
    {
        //GPS가 켜져있는지 체크
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(mCtx, "GPS활성화 필요 !!", Toast.LENGTH_SHORT).show();
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }
    }



    private void initComponents() {

        // Context
        mCtx = this;

        //db helper
        db = new DatabaseHelper(mCtx);

        mYearMonthDay = (TextView) findViewById(R.id.show_day);
        mHourMinute = (TextView) findViewById(R.id.show_time);

        // 음성 및 날씨 정보 얻는 버튼
        mNewton = (ImageButton) findViewById(R.id.newtone);
        mNewton.setOnClickListener(this);


        // Weather사용 관련 GPS정보 얻기 위한 Manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        alarmRequestCode = getIntent().getIntExtra("requestCode", -1);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /* SK Weather API Key 변수 저장. */
        API_KEY = getString(R.string.weather_key);


        /* 음성 합성 설정 */
        TextToSpeechManager.getInstance().initializeLibrary(getApplicationContext());

        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_DIALOG_BRIGHT)  //TTS 음색 모드 설정(여성 차분한 낭독체)
                .setListener(this)
                .build();


    }

    /* 날씨 읽어주기 시작 */
    public void startWeather () {


        Log.i(TAG , "startWeather");

        requestLocation();
    }

    /* 알람 소리 울리기 */
    public void startRington() {
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        player = new MediaPlayer();

        try {
            player.setDataSource(this, alert);
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);


        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {



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


    // 진동 시작
    public void startVibrator() {
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 진동 패턴
        long[] vibrate_pattern = {100, 300, 100, 700, 100, 300, 100, 700};
        vibe.vibrate(vibrate_pattern, 0);
    }

    // UIHandler관련 부분,.
    public void Handler (final Handler kindOfHandler) {
        new Thread()
        {
            @Override
            public void run() {

                Message msg = kindOfHandler.obtainMessage();
                kindOfHandler.sendMessage(msg);
            }
        }.start();

    }

    //  Kakao 음성 인식 시장.
    public void startUsingSpeechSDK() {

        // SDK 초기화 부분.
        SpeechRecognizerManager.getInstance().initializeLibrary(mCtx);

        // 클라이언트 생성
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WORD)
                .setUserDictionary("알람\n꺼줘\n꺼\n오프")
                .setGlobalTimeOut(30);

        client = builder.build();

        client.setSpeechRecognizeListener(new SpeechRecognizeListener() {
            @Override
            public void onReady() {

                Log.i(TAG + " onReady", "onReady");
                Handler(falseUIHandler);
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

                if(errorCode == 4){
                    Handler(trueUIHandler);
                    client.cancelRecording();

                    Timer timer = new Timer();
                    timer.schedule(new CustomTimer() , 1000);
                }

                Log.i(TAG + " onError", errorCode + "");
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

                        Handler(falseUIHandler);
                        mNotificationManager.cancel(alarmRequestCode);

                        Log.i(TAG + " :: MEMO :: " , record.getMemo().toLowerCase());

                        if (!("".equals(record.getMemo().toString()))) {
                            readMemo();
                        }

                        startWeather();
                    }
                }


                Log.i(TAG + "Partial", partialResult);
            }

            /* 카카오 API 음성 합성이 모두 끝났을 경우 . 동기로 돌아감. */
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
            }

            @Override
            public void onAudioLevel(float audioLevel) {
                Log.i(TAG + "AudioLevel", "onAudioLevel");
            }

            @Override
            public void onFinished() {

                Log.i(TAG + " onFinished", "onFinished");
                Handler(trueUIHandler);
            }
        });

        client.startRecording(false);
    }

    @Override
    public void onFinished() {
        int intSentSize = ttsClient.getSentDataSize();      //세션 중에 전송한 데이터 사이즈
        int intRecvSize = ttsClient.getReceivedDataSize();  //세션 중에 전송받은 데이터 사이즈

        final String strInacctiveText = "handleFinished() SentSize : " + intSentSize + "  RecvSize : " + intRecvSize;

        Log.i(TAG, strInacctiveText);


        if(TTSFlag) {
            finish();
        }
    }

    @Override
    public void onError(int code, String message) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 종료시점
        // API를 더이상 사용하지 않을 때 finalizeLibrary() 호출.
        SpeechRecognizerManager.getInstance().finalizeLibrary();

        //음성 합성 종료
        TextToSpeechManager.getInstance().finalizeLibrary();

        if (vibe != null) {
            vibe.cancel();
            vibe = null;
        }

        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class CustomTimer extends TimerTask{
        @Override
        public void run() {
            startUsingSpeechSDK();
        }
    }
}

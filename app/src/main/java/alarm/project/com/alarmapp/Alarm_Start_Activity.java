package alarm.project.com.alarmapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;

import java.util.ArrayList;

public class Alarm_Start_Activity extends AppCompatActivity {

    private final String TAG = "Alarm_Start";

    private Context mCtx;
    private SpeechRecognizerClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm__start_);

        initComponents();

        startUsingSpeechSDK();
    }

    private void initComponents() {
        mCtx = this;
    }

    public void startUsingSpeechSDK(){
        // SDK 초기화 부분.
        SpeechRecognizerManager.getInstance().initializeLibrary(mCtx);

        // 클라이언트 생성
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_DICTATION );

        client = builder.build();

        client.setSpeechRecognizeListener(new SpeechRecognizeListener() {
            @Override
            public void onReady() {
                Log.i(TAG + " onReady" , "onReady");
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onEndOfSpeech() {
                Log.i(TAG + "EndOfSpeech" , "onEndOfSpeech");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.i(TAG + " onError" , errorMsg);
            }

            @Override
            public void onPartialResult(String partialResult) {
                Log.i(TAG + "Partial" , partialResult);
            }

            @Override
            public void onResults(Bundle results) {
                Log.i(TAG + " onResults" , "onResults");

                ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
                ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);
                Log.i(TAG + " text.size" , texts.size() + "");
                Log.i(TAG + " confs.size" , confs.size() + "");
                for (int i = 0; i < texts.size(); i++){
                    Log.i(TAG + " :: text " + i , texts.get(i).toString());
                    Log.i(TAG + " :: confs " + i , confs.get(i).toString());
                }

            }

            @Override
            public void onAudioLevel(float audioLevel) {
                Log.i(TAG + "AudioLevel" , "onAudioLevel");
            }

            @Override
            public void onFinished() {
                Log.i(TAG + " onFinished" , "onFinished");
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

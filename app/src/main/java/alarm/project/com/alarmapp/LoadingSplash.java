package alarm.project.com.alarmapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class LoadingSplash extends Activity {


    private static final int REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_splash);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);
            } else {
                // 유저가 거부하면서 다시 묻지 않기를 클릭5.. 권한이 없다고 유저에게 직접 알림.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);


            }
        } else {
            Handler hd = new Handler();
            hd.postDelayed(new splashhandler(), 2000); // 1초 후에 hd handler 실행  2000ms = 3초
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Handler hd = new Handler();
                    hd.postDelayed(new splashhandler(), 2000); // 1초 후에 hd handler 실행  2000ms = 3초
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private class splashhandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(), MainActivity.class)); //로딩이 끝난 후, ChoiceFunction 이동
            LoadingSplash.this.finish(); // 로딩페이지 Activity stack에서 제거
        }
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }
}

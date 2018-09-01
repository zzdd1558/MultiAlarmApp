package alarm.project.com.alarmapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import alarm.project.com.alarmapp.adapter.AlarmAdapter;
import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;
import alarm.project.com.alarmapp.utils.SharedPrefsUtils;

import static com.kakao.util.helper.Utility.getPackageInfo;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    // DB Helper
    private DatabaseHelper db = null;

    // Context
    private Context mCtx = null;

    private static RecyclerView mAlarmRecyclerView = null ;
    private static RecyclerView.LayoutManager mLayoutManager = null;

    // toolbar 및 floatAction Button
    private Toolbar toolbar = null;
    private FloatingActionButton fab = null;

    private static AlarmAdapter alarmAdapter = null;

    @Override
    protected void onStart() {
        super.onStart();
        RefreshView.recyclerViewRefresh(db.selectAll());
    }


    /*
     * 삭제가 단일 Activity에서 이루어 지기때문에 static로 설정후
     * RecyclerView 다시 그리기.
    */
    public static class RefreshView{
         public static void recyclerViewRefresh(List<AlarmRecordDTO> list){
             List<AlarmRecordDTO> recycle_list = list;

             alarmAdapter = new AlarmAdapter(recycle_list);
             mAlarmRecyclerView.setAdapter(alarmAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("KEY_HASH" , getKeyHash(this));
        initComponents();

        //db.onDelete();

        toolbar.setTitle("알람 앱");
        setSupportActionBar(toolbar);

        // 알람 등록 버튼
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SetAlarm.class);
                intent.putExtra("method","insert");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.stay);
            }
        });
    }

    // Component 초기화.
    public void initComponents(){

        // Context
        mCtx = this;

        // Recycler설정 초기화 .
        mAlarmRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mAlarmRecyclerView.setHasFixedSize(true);

        // Layout 설정 초기화.
        mLayoutManager = new LinearLayoutManager(this);
        mAlarmRecyclerView.setLayoutManager(mLayoutManager);

        // toolbar 및 floatingAction button 설정
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        //db helper
        db = new DatabaseHelper(mCtx);


    }

    /**
     *  Kakao 개발자 센터 앱에 등록할 Hash Key를 얻는 부분
     *  각각 다른 PC에서 실행할때마다 바뀌기 때문에
     *  Hash키를 다시 등록해줘야 한다.
     * */
    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("hello", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }
}

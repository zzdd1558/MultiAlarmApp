package alarm.project.com.alarmapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import alarm.project.com.alarmapp.adapter.AlarmAdapter;
import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;
import alarm.project.com.alarmapp.utils.SharedPrefsUtils;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    // DB Utils
    private DatabaseHelper db = null;

    // Context
    private Context mCtx = null;

    private RecyclerView mAlarmRecyclerView = null ;
    private RecyclerView.LayoutManager mLayoutManager = null;

    // toolbar 및 floatAction Button
    private Toolbar toolbar = null;
    private FloatingActionButton fab = null;

    private AlarmAdapter alarmAdapter = null;

    @Override
    protected void onStart() {
        super.onStart();

        List<AlarmRecordDTO> list = db.selectAll();
        Log.i(TAG , "===========");

        for ( AlarmRecordDTO data : list){
            Log.i(TAG , data.toString());
        }
        Log.i(TAG , "===========");

        alarmAdapter = new AlarmAdapter(list);
        mAlarmRecyclerView.setAdapter(alarmAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();

        //db.onDelete();

        toolbar.setTitle("알람 앱");
        setSupportActionBar(toolbar);


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

    public void initComponents(){

        // Context
        mCtx = this;

        mAlarmRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mAlarmRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mAlarmRecyclerView.setLayoutManager(mLayoutManager);

        // toolbar 및 floatingAction button 설정
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        //db helper
        db = new DatabaseHelper(mCtx);

        // BroadCast PendingIntent시 화면을 켜주기 위한 설정. 및 잠금화면 위에서 Activity 실행.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }
}

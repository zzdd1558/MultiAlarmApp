package alarm.project.com.alarmapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import alarm.project.com.alarmapp.R;
import alarm.project.com.alarmapp.SetAlarm;
import alarm.project.com.alarmapp.helper.DatabaseHelper;
import alarm.project.com.alarmapp.models.AlarmRecordDTO;

public class AlarmAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "AlarmAdapter";

    List<AlarmRecordDTO> recordList = null;

    public void AlarmAdapter() {

    }

    public AlarmAdapter(List<AlarmRecordDTO> list) {
        this.recordList = list;
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
                    db.soundCheck(alarmYN.isChecked() == true ? "Y" : "N" , alarmRequestCode);
                    break;
            }
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

package alarm.project.com.alarmapp.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import alarm.project.com.alarmapp.models.AlarmRecordDTO;

public class DatabaseHelper extends SQLiteOpenHelper{

    // TAG Label
    private static String TAG = "DatabaseHelper";

    // Options
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Alarm";
    private static final String TABLE_NAME = "Alarm_DB";

    // Columns
    private static final String columnCode = "request_code";
    private static final String columnTime = "regist_time";
    private static final String columnSound = "alarm_sound";
    private static final String columnYN = "alarm_flag";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // DB Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder create_query = new StringBuilder();
        create_query.append("CREATE TABLE ");
        create_query.append(TABLE_NAME);
        create_query.append("(");
        create_query.append(columnCode + " INTEGER PRIMARY KEY ," );
        create_query.append(columnTime + " TEXT NOT NULL , ");
        create_query.append(columnSound + " INTEGER NOT NULL , ");
        create_query.append(columnYN + " TEXT NOT NULL );");

        db.execSQL(create_query.toString());

    }

    // DB 버전 업그레이드
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        StringBuilder drop_query = new StringBuilder();
        drop_query.append("DROP TABLE IF EXISTS ");
        drop_query.append(TABLE_NAME);

        db.execSQL(drop_query.toString());

        onCreate(db);
    }

    // DB 데이터 삽입
    public void onInsert(AlarmRecordDTO data) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues inputValues = new ContentValues();
        inputValues.put(columnCode , data.getRequestCode());
        inputValues.put(columnTime , data.getRegistTime());
        inputValues.put(columnSound , data.getAlarmSound());
        inputValues.put(columnYN , data.getAlarmFlag());

        db.insert(TABLE_NAME , null , inputValues);
        db.close();
    }

    // DB 데이터 Select
    public List<AlarmRecordDTO> selectAll(){

        List<AlarmRecordDTO> alarmList = new ArrayList<AlarmRecordDTO>();

        StringBuilder select_query = new StringBuilder();
        select_query.append("SELECT * FROM ");
        select_query.append(TABLE_NAME);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(select_query.toString() , null);



        if (cursor.moveToFirst()) {
            do{
                AlarmRecordDTO record = new AlarmRecordDTO();
                record.setRequestCode(Integer.parseInt(cursor.getString(0)));
                record.setRegistTime(cursor.getString(1));
                record.setAlarmSound(Integer.parseInt(cursor.getString(2)));
                record.setAlarmFlag(cursor.getString(3));

                alarmList.add(record);
            }while (cursor.moveToNext());
        }
        return alarmList;
    }

    public  void onDelete(){
        SQLiteDatabase db = this.getWritableDatabase();

        StringBuilder drop_query = new StringBuilder();
        drop_query.append("DROP TABLE IF EXISTS ");
        drop_query.append(TABLE_NAME);

        db.execSQL(drop_query.toString());

        onCreate(db);
    }

    public void soundCheck (String flag , int requestCode){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(columnYN , flag);

        db.update(TABLE_NAME , values , columnCode + " = ? " , new String[]{String.valueOf(requestCode)});

    }

    public void onUpdate(AlarmRecordDTO data){
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(columnTime , data.getRegistTime());
        values.put(columnSound , data.getAlarmSound());
        values.put(columnYN , data.getAlarmFlag());

        db.update(TABLE_NAME , values , columnCode + " = ?" , new String[]{String.valueOf(data.getRequestCode())});
    }

    public void onDeleteOne(int requestCode){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME , columnCode + " = ?" , new String[]{String.valueOf(requestCode)});

    }

    public AlarmRecordDTO onSelectOne(int requestCode){
        SQLiteDatabase db = this.getWritableDatabase();

        StringBuilder selectOneQuery = new StringBuilder();
        selectOneQuery.append("SELECT * FROM ");
        selectOneQuery.append(TABLE_NAME);
        selectOneQuery.append(" WHERE ");
        selectOneQuery.append(columnCode);
        selectOneQuery.append(" = ");
        selectOneQuery.append(requestCode);

       Cursor cursor = db.rawQuery(selectOneQuery.toString() , null);
        AlarmRecordDTO record = null;
        if (cursor.moveToFirst()) {
            do{
                record = new AlarmRecordDTO();
                record.setRequestCode(Integer.parseInt(cursor.getString(0)));
                record.setRegistTime(cursor.getString(1));
                record.setAlarmSound(Integer.parseInt(cursor.getString(2)));
                record.setAlarmFlag(cursor.getString(3));
            }while (cursor.moveToNext());
        }
        Log.i(TAG , record.toString());
        return record;

    }
}

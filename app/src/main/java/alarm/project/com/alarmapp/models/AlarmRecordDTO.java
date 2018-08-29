package alarm.project.com.alarmapp.models;

public class AlarmRecordDTO {
                                        // db column
    private int requestCode;            // request_code
    private String registTime = null;   // regist_time
    private int alarmSound;             // alarm_sound
    private String alarmFlag = null;    // alarm_flag


    public AlarmRecordDTO() {
    }

    public AlarmRecordDTO(int requestCode, String registTime, int alarmSound, String alarmFlag) {
        this.requestCode = requestCode;
        this.registTime = registTime;
        this.alarmSound = alarmSound;
        this.alarmFlag = alarmFlag;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public String getRegistTime() {
        return registTime;
    }

    public void setRegistTime(String registTime) {
        this.registTime = registTime;
    }

    public int getAlarmSound() {
        return alarmSound;
    }

    public void setAlarmSound(int alarmSound) {
        this.alarmSound = alarmSound;
    }

    public String getAlarmFlag() {
        return alarmFlag;
    }

    public void setAlarmFlag(String alarmFlag) {
        this.alarmFlag = alarmFlag;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AlarmRecordDTO{");
        sb.append("requestCode=").append(requestCode);
        sb.append(", registTime='").append(registTime).append('\'');
        sb.append(", alarmSound=").append(alarmSound);
        sb.append(", alarmFlag='").append(alarmFlag).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
package alarm.project.com.alarmapp.utils;

public class TimeSplitUtils {
    public TimeSplitUtils() {}

    public static String[] timeSplit(String data , String oper) {
        return data.split(oper);
    }

    public static int[] calTimeSplit (String dbTime){

        String[] timeSplit = TimeSplitUtils.timeSplit(dbTime , "-");
        String[] hour_minute_split = TimeSplitUtils.timeSplit(timeSplit[1],":");
        String[] month_split = TimeSplitUtils.timeSplit(timeSplit[2] , "월");
        String[] day_split = TimeSplitUtils.timeSplit(month_split[1] , "일");

        // 년 월 일 시간 분 필요.
        int mYear = Integer.parseInt(timeSplit[3]);
        int mMonth = Integer.parseInt(month_split[0]);
        int mDayOfMonth = Integer.parseInt(day_split[0].trim());
        int mHour = Integer.parseInt(hour_minute_split[0]);
        int mMinute = Integer.parseInt(hour_minute_split[1]);


        return new int[]{mYear , mMonth , mDayOfMonth , mHour , mMinute};
    }
}

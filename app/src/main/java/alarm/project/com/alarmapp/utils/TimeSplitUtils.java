package alarm.project.com.alarmapp.utils;

public class TimeSplitUtils {
    public TimeSplitUtils() {}

    public static String[] timeSplit(String data , String oper) {
        return data.split(oper);
    }
}

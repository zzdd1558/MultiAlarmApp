package alarm.project.com.alarmapp.models;

public class AlarmVO {

    /* 년 월 일 */
    int year;
    int month;
    int dayOfMonth;

    /* 시간 분 */
    int hour;
    int minute;

    /* 기본 생성자*/
    public AlarmVO() {    }

    /*
    * year              년
    * month             월
    * dayOfMonth        일
    * hour              시간
    * minute            분
    * */
    public AlarmVO(int year, int month, int dayOfMonth, int hour, int minute) {
        this.year =         year;
        this.month =        month;
        this.dayOfMonth =   dayOfMonth;
        this.hour =         hour;
        this.minute =       minute;
    }


    /* Setter / Getter */
    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AlarmVO{");
        sb.append("year=").append(year);
        sb.append(", month=").append(month);
        sb.append(", dayOfMonth=").append(dayOfMonth);
        sb.append(", hour=").append(hour);
        sb.append(", minute=").append(minute);
        sb.append('}');
        return sb.toString();
    }
}

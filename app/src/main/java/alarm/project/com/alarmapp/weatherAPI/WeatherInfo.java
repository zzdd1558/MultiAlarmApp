package alarm.project.com.alarmapp.weatherAPI;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ybkim on 2018-08-27.
 */

public class WeatherInfo {
    @SerializedName("result")
    Result result;
    @SerializedName("weather")
    weather weather;
    @SerializedName("common")
    Common common;

    public class Result {
        @SerializedName("message")
        String message; // 메시지
        @SerializedName("code")
        String code; // 요청결과

        public String getMessage() {return message;}
        public String getCode() {return code;}
    }

    public class Common {
        @SerializedName("alertYn")
        String alertYn; // 특보 존재유무(한반도 전역 기준) - Y:특보있음, N: 특보없음
        @SerializedName("stormYn")
        String stormYn; // 태풍 존재유무(경계구역 기준) - Y:태풍있음, N:태풍없음

        public String getAlertYn() {return alertYn;}
        public String getStormYn() {return stormYn;}
    }

    public class weather {
        public List<hourly> hourly = new ArrayList<>();
        public List<hourly> getHourly() {return hourly;}

        public class hourly {
            @SerializedName("humidity")
            String humidity; // 습도
            @SerializedName("lightning")
            String lightning; // 낙뢰유무(해당격자내) - 0:없음, - 1:있음
            @SerializedName("timeRelease")
            String timeRelease; // 발표시간
            @SerializedName("sunRiseTime")
            String sunRiseTime; // 일출시간
            @SerializedName("sunSetTime")
            String sunSetTime; // 일몰시간

            @SerializedName("grid") Grid grid;
            @SerializedName("sky") Sky sky;
            @SerializedName("precipitation") precipitation precipitation;
            @SerializedName("temperature") temperature temperature;
            @SerializedName("wind") wind wind;

            public class Grid{
                @SerializedName("city")
                String city;
                @SerializedName("village")
                String village;
                @SerializedName("county")
                String county;
                @SerializedName("longitude")
                String longitude;
                @SerializedName("latitude")
                String latitude;

                public String getCity() {return city;}
                public String getVillage() {return village;}
                public String getCounty() {return county;}
                public String getLongitude() {return longitude;}
                public String getLatitude() {return latitude;}
            }

            public class Sky{
                @SerializedName("name")
                String name; // 하늘상태 코드명(맑음, 흐림 등)
                @SerializedName("code")
                String code; // 하늘상태 코드

                public String getName() {return name;}
                public String getCode() {return code;}
            }

            public class precipitation{ // 강수 정보
                @SerializedName("sinceOntime")
                String sinceOntime; // 강우 또는 적설량(type:0,1,2 강우량 type:3 적설량)
                @SerializedName("type")
                String type; //0 :없음 1:비 2: 비/눈 3: 눈

                public String getSinceOntime() {return sinceOntime;}
                public String getType() {return type;}
            }
            public class temperature{
                @SerializedName("tc")
                String tc; // 현재 기온
                @SerializedName("tmax")
                String tmax; // 오늘의 최고기온
                @SerializedName("tmin")
                String tmin; // 오늘의 최저기온

                public String getTc() {return tc;}
                public String getTmax() {return tmax;}
                public String getTmin() {return tmin;}
            }
            public class wind{ // 바람
                @SerializedName("wdir")
                String wdir; // 풍향(degree)
                @SerializedName("wspd")
                String wspd; // 풍속(m/s)

                public String getWdir() {return wdir;}
                public String getWspd() {return wspd;}
            }
            public hourly.Grid getGrid() {return grid;}
            public hourly.Sky getSky() {return sky;}
            public hourly.precipitation getPrecipitation() {return precipitation;}
            public hourly.temperature getTemperature() {return temperature;}
            public hourly.wind getWind() {return wind;}
            public String getHumidity() {return humidity;}
        }
    }
    public Common getCommon() {return common;}
    public Result getResult() {return result;}
    public weather getWeather() {return weather;}
}
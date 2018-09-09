package alarm.project.com.alarmapp.weatherAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by ybkim on 2018-05-28.
 */

public interface ApiService {

    /* SK Weather API에서 불러올 경로 */
    // appKey와 version , lat , lon  앱키 , 버전 , 위*경도를 같이 매개변수에 태워서 보낸다.
    @GET("current/hourly")
    Call<WeatherInfo> getCurrentWeather(@Header("appKey") String appKey, @Query("version") int version,
                                        @Query("lat") double lat, @Query("lon") double lon);

}
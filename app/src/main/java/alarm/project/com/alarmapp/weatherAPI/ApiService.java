package alarm.project.com.alarmapp.weatherAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Created by ybkim on 2018-05-28.
 */

public interface ApiService {
    /*
   Retrofit get annotation with our URL
   And our method that will return us the List of Contacts
   */
    @GET("current/hourly")
    Call<WeatherInfo> getCurrentWeather(@Header("appKey") String appKey, @Query("version") int version,
                                        @Query("lat") double lat, @Query("lon") double lon);

}
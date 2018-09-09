package alarm.project.com.alarmapp.network;

import alarm.project.com.alarmapp.weatherAPI.ApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ybkim on 2018-05-28.
 */

public class RetroClient {

    /* SK Weather API REST 경로 */
    private static final String ROOT_URL = "https://api2.sktelecom.com/weather/";

    /* 기본 생성자 */
    public RetroClient() {
    }

    /**
     * Get Retro Client
     *
     * @return JSON Object
     */
    private static Retrofit getRetroClient() {
        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getApiService() {
        return getRetroClient().create(ApiService.class);
    }
}
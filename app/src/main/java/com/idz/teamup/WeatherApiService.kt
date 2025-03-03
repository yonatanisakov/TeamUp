import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v4/weather/forecast")
    fun getWeatherForecast(
        @Query("location") city: String,
        @Query("apikey") apiKey: String
    ): Call<WeatherResponse>
}

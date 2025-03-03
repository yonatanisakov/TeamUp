import android.util.Log
import com.idz.teamup.repository.GeoDBRepo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object WeatherRepo {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.tomorrow.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApiService::class.java)

    fun fetchWeather(city: String, eventDate: String, apiKey: String, onResult: (String) -> Unit) {
        val formattedDate = convertToISO8601(eventDate)

        api.getWeatherForecast(city, apiKey)
            .enqueue(object : retrofit2.Callback<WeatherResponse> {
                override fun onResponse(call: retrofit2.Call<WeatherResponse>, response: retrofit2.Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weather = response.body()
                        val dailyForecast = weather?.timelines?.daily?.find { it.time.startsWith(formattedDate.substring(0, 10)) }

                        if (dailyForecast != null) {
                            val temp = dailyForecast.values.temperatureAvg
                            val weatherCondition = getWeatherDescription(dailyForecast.values.weatherCodeMax)

                            val weatherResult = "Weather: $weatherCondition, ${temp}°C"
                            onResult(weatherResult)
                        } else {
                            onResult("Weather data is only available **one week before** the event. Check back later! 📅")
                        }
                    } else {
                        onResult("Couldn't fetch weather. Try again later! ❌")
                    }
                }

                override fun onFailure(call: retrofit2.Call<WeatherResponse>, t: Throwable) {
                    onResult("Network error. Check your connection and try again! 📡")
                }
            })
    }





    private fun convertToISO8601(date: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(date) ?: Date())
    }


    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            1000 -> "Clear Sky ☀️"
            1001 -> "Cloudy ☁️"
            1100 -> "Mostly Clear 🌤"
            1101 -> "Partly Cloudy ⛅"
            1102 -> "Mostly Cloudy 🌥"
            2000 -> "Fog 🌫"
            2100 -> "Light Fog 🌫"
            4000 -> "Drizzle 🌧"
            4200 -> "Light Rain 🌦"
            4201 -> "Heavy Rain 🌧"
            5001 -> "Light Snow ❄️"
            5100 -> "Snow ❄️"
            else -> ""
        }
    }
}

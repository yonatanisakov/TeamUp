data class WeatherResponse(
    val timelines: WeatherTimelines?
)

data class WeatherTimelines(
    val daily: List<DailyForecast>
)

data class DailyForecast(
    val time: String,
    val values: WeatherValues
)

data class WeatherValues(
    val temperatureAvg: Double,
    val weatherCodeMax: Int
)

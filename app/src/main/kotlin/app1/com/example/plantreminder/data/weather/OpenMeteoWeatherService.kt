package com.example.mmapp.app1.data.weather

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class WeatherLocationConfig(
    val queryName: String,
    val displayName: String,
    val countryCode: String,
    val timezone: String,
)

data class WeatherFetchResult(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val days: List<WeatherRemoteDay>,
)

data class WeatherRemoteDay(
    val date: LocalDate,
    val rainMm: Double,
    val precipitationMm: Double,
    val precipitationProbabilityMax: Int?,
    val precipitationHours: Double,
)

class OpenMeteoWeatherService(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun fetchDailyWeather(
        location: WeatherLocationConfig,
        today: LocalDate = LocalDate.now(),
    ): WeatherFetchResult {
        val geo = resolveLocation(location)
        val forecastUrl = buildString {
            append("https://api.open-meteo.com/v1/forecast")
            append("?latitude=${geo.latitude}")
            append("&longitude=${geo.longitude}")
            append("&daily=rain_sum,precipitation_sum,precipitation_probability_max,precipitation_hours")
            append("&past_days=3")
            append("&forecast_days=4")
            append("&timezone=${encode(location.timezone)}")
        }
        val response = get(forecastUrl)
        val parsed = json.decodeFromString<OpenMeteoForecastResponse>(response)
        val daily = parsed.daily ?: error("No se recibieron datos diarios de Open-Meteo.")
        val dates = daily.time
        if (dates.isEmpty()) error("Open-Meteo devolvió una serie temporal vacía.")

        val days = dates.indices.map { index ->
            WeatherRemoteDay(
                date = LocalDate.parse(dates[index]),
                rainMm = daily.rainSum.valueAt(index),
                precipitationMm = daily.precipitationSum.valueAt(index),
                precipitationProbabilityMax = daily.precipitationProbabilityMax?.valueAt(index)?.toInt(),
                precipitationHours = daily.precipitationHours.valueAt(index),
            )
        }.filter { day ->
            day.date in today.minusDays(3)..today.plusDays(3)
        }

        return WeatherFetchResult(
            locationName = geo.name.ifBlank { location.displayName },
            latitude = geo.latitude,
            longitude = geo.longitude,
            days = days,
        )
    }

    private fun resolveLocation(location: WeatherLocationConfig): OpenMeteoLocationResult {
        val geocodingUrl = buildString {
            append("https://geocoding-api.open-meteo.com/v1/search")
            append("?name=${encode(location.queryName)}")
            append("&count=1")
            append("&language=es")
            append("&countryCode=${encode(location.countryCode)}")
        }
        val response = get(geocodingUrl)
        val parsed = json.decodeFromString<OpenMeteoGeocodingResponse>(response)
        return parsed.results.orEmpty().firstOrNull()
            ?: error("No se encontró la ubicación ${location.displayName} en Open-Meteo.")
    }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/json")
        return try {
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}

private fun List<Double?>?.valueAt(index: Int): Double = this?.getOrNull(index) ?: 0.0

@Serializable
private data class OpenMeteoGeocodingResponse(
    val results: List<OpenMeteoLocationResult>? = null,
)

@Serializable
private data class OpenMeteoLocationResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
private data class OpenMeteoForecastResponse(
    val daily: OpenMeteoDailyResponse? = null,
)

@Serializable
private data class OpenMeteoDailyResponse(
    val time: List<String> = emptyList(),
    @SerialName("rain_sum")
    val rainSum: List<Double?>? = null,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double?>? = null,
    @SerialName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Double?>? = null,
    @SerialName("precipitation_hours")
    val precipitationHours: List<Double?>? = null,
)

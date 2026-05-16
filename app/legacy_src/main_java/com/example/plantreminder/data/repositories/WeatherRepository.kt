package com.example.plantreminder.data.repositories

import com.example.plantreminder.data.db.WeatherDayDao
import com.example.plantreminder.data.db.WeatherDayEntity
import com.example.plantreminder.data.weather.OpenMeteoWeatherService
import com.example.plantreminder.data.weather.WeatherLocationConfig
import com.example.plantreminder.domain.models.WeatherDay
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherDayDao: WeatherDayDao,
    private val weatherService: OpenMeteoWeatherService,
    private val location: WeatherLocationConfig = WeatherLocationConfig(
        queryName = "Rafelbunyol",
        displayName = "Rafelbunyol",
        countryCode = "ES",
        timezone = "Europe/Madrid",
    ),
) : WeatherDataSource {
    override fun observeWeather(): Flow<List<WeatherDay>> = weatherDayDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getWeatherDays(): List<WeatherDay> = withContext(Dispatchers.IO) {
        weatherDayDao.getAll().map { it.toDomain() }
    }

    override suspend fun refreshWeather(): WeatherRefreshResult {
        return withContext(Dispatchers.IO) {
            val fetchedAt = System.currentTimeMillis()
            val result = weatherService.fetchDailyWeather(location)
            val entities = result.days.map { day ->
                WeatherDayEntity(
                    date = day.date.toString(),
                    locationName = result.locationName,
                    latitude = result.latitude,
                    longitude = result.longitude,
                    rainMm = day.rainMm,
                    precipitationMm = day.precipitationMm,
                    precipitationProbabilityMax = day.precipitationProbabilityMax,
                    precipitationHours = day.precipitationHours,
                    provider = "open-meteo",
                    fetchedAt = fetchedAt,
                )
            }
            weatherDayDao.deleteAll()
            weatherDayDao.insertAll(entities)
            WeatherRefreshResult(
                locationName = result.locationName,
                dayCount = entities.size,
                fetchedAt = fetchedAt,
            )
        }
    }
}

private fun WeatherDayEntity.toDomain(): WeatherDay = WeatherDay(
    date = LocalDate.parse(date),
    locationName = locationName,
    latitude = latitude,
    longitude = longitude,
    rainMm = rainMm,
    precipitationMm = precipitationMm,
    precipitationProbabilityMax = precipitationProbabilityMax,
    precipitationHours = precipitationHours,
    provider = provider,
    fetchedAt = fetchedAt,
)

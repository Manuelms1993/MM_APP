package com.example.plantreminder.data.repositories

import com.example.plantreminder.domain.models.WeatherDay
import kotlinx.coroutines.flow.Flow

interface WeatherDataSource {
    fun observeWeather(): Flow<List<WeatherDay>>

    suspend fun getWeatherDays(): List<WeatherDay>

    suspend fun refreshWeather(): WeatherRefreshResult
}

data class WeatherRefreshResult(
    val locationName: String,
    val dayCount: Int,
    val fetchedAt: Long,
)

package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.WeatherDataSource
import com.example.plantreminder.data.repositories.WeatherRefreshResult

class RefreshWeatherUseCase(
    private val weatherDataSource: WeatherDataSource,
) {
    suspend operator fun invoke(): WeatherRefreshResult = weatherDataSource.refreshWeather()
}

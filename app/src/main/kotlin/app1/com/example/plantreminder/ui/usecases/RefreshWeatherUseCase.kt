package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.WeatherDataSource
import com.example.mmapp.app1.data.repositories.WeatherRefreshResult

class RefreshWeatherUseCase(
    private val weatherDataSource: WeatherDataSource,
) {
    suspend operator fun invoke(): WeatherRefreshResult = weatherDataSource.refreshWeather()
}

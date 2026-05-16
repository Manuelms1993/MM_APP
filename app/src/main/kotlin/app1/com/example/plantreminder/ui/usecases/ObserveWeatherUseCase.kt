package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.WeatherDataSource

class ObserveWeatherUseCase(
    private val weatherDataSource: WeatherDataSource,
) {
    operator fun invoke() = weatherDataSource.observeWeather()
}

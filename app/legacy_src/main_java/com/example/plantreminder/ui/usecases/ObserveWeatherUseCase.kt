package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.WeatherDataSource

class ObserveWeatherUseCase(
    private val weatherDataSource: WeatherDataSource,
) {
    operator fun invoke() = weatherDataSource.observeWeather()
}

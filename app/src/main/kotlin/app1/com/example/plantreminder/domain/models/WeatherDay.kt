package com.example.mmapp.app1.domain.models

import java.time.LocalDate

data class WeatherDay(
    val date: LocalDate,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val rainMm: Double,
    val precipitationMm: Double,
    val precipitationProbabilityMax: Int?,
    val precipitationHours: Double,
    val provider: String,
    val fetchedAt: Long,
)

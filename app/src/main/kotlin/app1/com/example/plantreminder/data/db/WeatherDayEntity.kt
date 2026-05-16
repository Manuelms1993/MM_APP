package com.example.mmapp.app1.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_days")
data class WeatherDayEntity(
    @PrimaryKey val date: String,
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

package com.example.plantreminder.domain.models

data class WateringRule(
    val frequenciesBySeason: SeasonalFrequency,
    val notes: List<String>,
)


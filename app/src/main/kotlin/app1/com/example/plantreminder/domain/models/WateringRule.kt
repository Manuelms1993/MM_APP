package com.example.mmapp.app1.domain.models

data class WateringRule(
    val frequenciesBySeason: SeasonalFrequency,
    val notes: List<String>,
)


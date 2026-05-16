package com.example.plantreminder.domain.models

data class SeasonalFrequency(
    val frequenciesBySeason: Map<Season, Int>,
) {
    fun forSeason(season: Season): Int? = frequenciesBySeason[season]
}


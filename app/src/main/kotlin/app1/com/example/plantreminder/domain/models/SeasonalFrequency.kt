package com.example.mmapp.app1.domain.models

data class SeasonalFrequency(
    val frequenciesBySeason: Map<Season, Int>,
) {
    fun forSeason(season: Season): Int? = frequenciesBySeason[season]
}


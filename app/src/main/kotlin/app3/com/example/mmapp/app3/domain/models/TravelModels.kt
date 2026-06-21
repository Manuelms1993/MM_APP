package com.example.mmapp.app3.domain.models

data class TravelGuide(
    val days: List<TravelDay>,
    val hotels: List<TravelHotel>,
    val hotelsById: Map<String, TravelHotel>,
)

data class TravelDay(
    val id: String,
    val dayNumber: Int,
    val title: String,
    val city: String,
    val summary: String,
    val overnightHotelId: String?,
    val segments: List<DaySegment>,
    val foodSuggestions: List<String>,
    val notes: List<String>,
    val recommendations: List<TravelRecommendation>,
    val links: List<TravelLink>,
)

data class DaySegment(
    val title: String,
    val timeLabel: String?,
    val bullets: List<String>,
    val topics: List<TravelTopic>,
    val references: List<String>,
    val links: List<TravelLink>,
)

data class TravelTopic(
    val title: String,
    val bullets: List<String>,
    val topics: List<TravelTopic>,
    val links: List<TravelLink>,
)

data class TravelHotel(
    val id: String,
    val name: String,
    val city: String,
    val days: List<Int>,
    val address: String,
    val status: String,
    val notes: List<String>,
    val sourceUrl: String?,
)

data class TravelRecommendation(
    val text: String,
    val links: List<TravelLink>,
)

data class TravelLink(
    val label: String,
    val url: String,
)

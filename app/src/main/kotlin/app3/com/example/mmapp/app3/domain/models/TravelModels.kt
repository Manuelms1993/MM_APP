package com.example.mmapp.app3.domain.models

import java.time.LocalDate

data class TravelGuide(
    val days: List<TravelDay>,
    val hotels: List<TravelHotel>,
    val hotelsById: Map<String, TravelHotel>,
)

data class TravelDay(
    val id: String,
    val dayNumber: Int,
    val date: LocalDate,
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
    val references: List<String>,
    val links: List<TravelLink>,
)

data class TravelHotel(
    val id: String,
    val name: String,
    val city: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val address: String,
    val status: String,
    val priceLabel: String,
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

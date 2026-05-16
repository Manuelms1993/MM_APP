package com.example.mmapp.app3.data.input

import com.example.mmapp.app3.domain.models.DaySegment
import com.example.mmapp.app3.domain.models.TravelDay
import com.example.mmapp.app3.domain.models.TravelGuide
import com.example.mmapp.app3.domain.models.TravelHotel
import com.example.mmapp.app3.domain.models.TravelLink
import com.example.mmapp.app3.domain.models.TravelRecommendation
import java.time.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TravelGuideParser(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun parse(
        dayFiles: List<RawTravelInput>,
        hotelsFile: RawTravelInput?,
    ): TravelGuide {
        val hotels = hotelsFile?.let(::parseHotels).orEmpty()
        val hotelsById = hotels.associateBy { it.id }
        val days = dayFiles
            .map(::parseDay)
            .sortedWith(compareBy<TravelDay> { it.date }.thenBy { it.dayNumber })

        require(days.isNotEmpty()) { "No hay días de viaje cargados." }

        return TravelGuide(
            days = days,
            hotels = hotels,
            hotelsById = hotelsById,
        )
    }

    private fun parseHotels(input: RawTravelInput): List<TravelHotel> {
        val dto = json.decodeFromString<TravelHotelsDto>(input.rawJson)
        return dto.hotels.map { hotel ->
            TravelHotel(
                id = hotel.id,
                name = hotel.name,
                city = hotel.city,
                checkIn = LocalDate.parse(hotel.checkIn),
                checkOut = LocalDate.parse(hotel.checkOut),
                address = hotel.address,
                status = hotel.status,
                priceLabel = hotel.priceLabel,
                notes = hotel.notes,
                sourceUrl = hotel.sourceUrl,
            )
        }
    }

    private fun parseDay(input: RawTravelInput): TravelDay {
        val dto = json.decodeFromString<TravelDayDto>(input.rawJson)
        return TravelDay(
            id = dto.id,
            dayNumber = dto.dayNumber,
            date = LocalDate.parse(dto.date),
            title = dto.title,
            city = dto.city,
            summary = dto.summary,
            overnightHotelId = dto.overnightHotelId,
            segments = dto.segments.map { segment ->
                DaySegment(
                    title = segment.title,
                    timeLabel = segment.timeLabel,
                    bullets = segment.bullets,
                    references = segment.references,
                    links = segment.links.map { TravelLink(it.label, it.url) },
                )
            },
            foodSuggestions = dto.foodSuggestions,
            notes = dto.notes,
            recommendations = dto.recommendations.map { recommendation ->
                TravelRecommendation(
                    text = recommendation.text,
                    links = recommendation.links.map { TravelLink(it.label, it.url) },
                )
            },
            links = dto.links.map { TravelLink(it.label, it.url) },
        )
    }
}

@Serializable
data class RawTravelInput(
    val fileName: String,
    val rawJson: String,
)

@Serializable
private data class TravelHotelsDto(
    val schemaVersion: Int = 1,
    val hotels: List<TravelHotelDto>,
)

@Serializable
private data class TravelHotelDto(
    val id: String,
    val name: String,
    val city: String,
    val checkIn: String,
    val checkOut: String,
    val address: String,
    val status: String,
    val priceLabel: String,
    val notes: List<String> = emptyList(),
    val sourceUrl: String? = null,
)

@Serializable
private data class TravelDayDto(
    val schemaVersion: Int = 1,
    val id: String,
    val dayNumber: Int,
    val date: String,
    val title: String,
    val city: String,
    val summary: String,
    val overnightHotelId: String? = null,
    val segments: List<TravelSegmentDto>,
    val foodSuggestions: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val recommendations: List<TravelRecommendationDto> = emptyList(),
    val links: List<TravelLinkDto> = emptyList(),
)

@Serializable
private data class TravelSegmentDto(
    val title: String,
    val timeLabel: String? = null,
    val bullets: List<String> = emptyList(),
    val references: List<String> = emptyList(),
    val links: List<TravelLinkDto> = emptyList(),
)

@Serializable
private data class TravelRecommendationDto(
    val text: String,
    val links: List<TravelLinkDto> = emptyList(),
)

@Serializable
private data class TravelLinkDto(
    val label: String,
    @SerialName("url") val url: String,
)

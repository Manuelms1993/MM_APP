package com.example.plantreminder.domain.usecases

import com.example.plantreminder.data.repositories.AppendResult
import com.example.plantreminder.data.repositories.ConversationDataSource
import com.example.plantreminder.data.repositories.PlantDefinitionDataSource
import com.example.plantreminder.data.repositories.WeatherDataSource
import com.example.plantreminder.domain.DailyActionCalculator
import com.example.plantreminder.domain.MessageBuilder
import com.example.plantreminder.domain.models.DailyMessage
import com.example.plantreminder.domain.models.MessageSource
import java.time.LocalDate

class GenerateDailyMessageUseCase(
    private val plantDefinitionRepository: PlantDefinitionDataSource,
    private val conversationRepository: ConversationDataSource,
    private val weatherDataSource: WeatherDataSource,
    private val dailyActionCalculator: DailyActionCalculator,
    private val messageBuilder: MessageBuilder,
) {
    suspend operator fun invoke(
        date: LocalDate,
        source: MessageSource,
    ): GenerateDailyMessageResult {
        if (conversationRepository.hasMessageForDate(date)) {
            return GenerateDailyMessageResult(
                status = DailyGenerationStatus.SKIPPED_EXISTING,
                dailyMessage = null,
            )
        }

        val plants = plantDefinitionRepository.getAllPlants()
        val weatherDays = weatherDataSource.getWeatherDays()
        val actions = dailyActionCalculator.calculate(date, plants, weatherDays)
        val dailyMessage = messageBuilder.build(date, actions)
        return when (
            conversationRepository.appendMessageForDate(
                dailyMessage = dailyMessage,
                source = source,
                status = com.example.plantreminder.domain.models.MessageStatus.GENERATED,
                createdAt = System.currentTimeMillis(),
            )
        ) {
            is AppendResult.Inserted -> GenerateDailyMessageResult(
                status = DailyGenerationStatus.GENERATED,
                dailyMessage = dailyMessage,
            )

            AppendResult.SkippedExisting -> GenerateDailyMessageResult(
                status = DailyGenerationStatus.SKIPPED_EXISTING,
                dailyMessage = null,
            )
        }
    }

    suspend fun markError(date: LocalDate) {
        conversationRepository.markError(date)
    }
}

enum class DailyGenerationStatus {
    GENERATED,
    SKIPPED_EXISTING,
}

data class GenerateDailyMessageResult(
    val status: DailyGenerationStatus,
    val dailyMessage: DailyMessage?,
)

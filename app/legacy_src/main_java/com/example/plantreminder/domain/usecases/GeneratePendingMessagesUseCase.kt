package com.example.plantreminder.domain.usecases

import com.example.plantreminder.data.repositories.ConversationDataSource
import com.example.plantreminder.data.repositories.PlantDefinitionDataSource
import com.example.plantreminder.domain.models.MessageSource
import java.time.LocalDate

class GeneratePendingMessagesUseCase(
    private val conversationRepository: ConversationDataSource,
    private val plantDefinitionRepository: PlantDefinitionDataSource,
    private val generateDailyMessageUseCase: GenerateDailyMessageUseCase,
) {
    suspend operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate = LocalDate.now(),
        source: MessageSource = MessageSource.MANUAL_BUTTON,
    ): PendingGenerationSummary {
        val resolvedStart = resolveStartDate(startDate, endDate)
        val recalculationStart = maxOf(resolvedStart, endDate.minusDays(6))
        conversationRepository.deleteBetween(recalculationStart, endDate)
        val generationDates = generateSequence(recalculationStart) { current ->
            current.plusDays(1).takeUnless { it.isAfter(endDate) }
        }.toList()
        var generatedCount = 0
        val errors = mutableListOf<PendingGenerationError>()

        generationDates.forEach { date ->
            try {
                when (generateDailyMessageUseCase(date = date, source = source).status) {
                    DailyGenerationStatus.GENERATED -> generatedCount++
                    DailyGenerationStatus.SKIPPED_EXISTING -> Unit
                }
            } catch (t: Throwable) {
                errors += PendingGenerationError(date, t.message ?: "Unknown error")
                generateDailyMessageUseCase.markError(date)
            }
        }

        return PendingGenerationSummary(
            generatedCount = generatedCount,
            skippedCount = 0,
            errors = errors,
        )
    }

    private suspend fun resolveStartDate(requestedStart: LocalDate?, endDate: LocalDate): LocalDate {
        requestedStart?.let { return it }
        return plantDefinitionRepository.getEarliestStartDate() ?: endDate
    }
}

data class PendingGenerationSummary(
    val generatedCount: Int,
    val skippedCount: Int,
    val errors: List<PendingGenerationError>,
)

data class PendingGenerationError(
    val date: LocalDate,
    val reason: String,
)

package com.example.mmapp.app2.domain.usecases

import com.example.mmapp.app2.data.repositories.ConversationDataSource
import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.models.MessageSource
import java.time.LocalDate

class GeneratePendingMessagesUseCase(
    private val conversationRepository: ConversationDataSource,
    private val menuCatalogDataSource: MenuCatalogDataSource,
    private val generateDailyMessageUseCase: GenerateDailyMessageUseCase,
) {
    suspend operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate = LocalDate.now().plusDays(DEFAULT_FUTURE_DAYS),
        source: MessageSource = MessageSource.MANUAL_BUTTON,
    ): PendingGenerationSummary {
        val resolvedStart = resolveStartDate(startDate, endDate)
        conversationRepository.deleteAll()

        val generationDates = generateSequence(resolvedStart) { current ->
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
        return LocalDate.now().takeIf { !it.isAfter(endDate) } ?: endDate
    }

    private companion object {
        const val DEFAULT_FUTURE_DAYS = 7L
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

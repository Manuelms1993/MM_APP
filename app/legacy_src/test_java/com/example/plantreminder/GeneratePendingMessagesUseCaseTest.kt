package com.example.plantreminder

import com.example.plantreminder.data.repositories.AppendResult
import com.example.plantreminder.data.repositories.ConversationDataSource
import com.example.plantreminder.data.repositories.PlantDefinitionDataSource
import com.example.plantreminder.data.repositories.WeatherDataSource
import com.example.plantreminder.data.repositories.WeatherRefreshResult
import com.example.plantreminder.domain.DailyActionCalculator
import com.example.plantreminder.domain.MessageBuilder
import com.example.plantreminder.domain.models.ConversationMessage
import com.example.plantreminder.domain.models.DailyMessage
import com.example.plantreminder.domain.models.FertilizerRule
import com.example.plantreminder.domain.models.MessageSource
import com.example.plantreminder.domain.models.MessageStatus
import com.example.plantreminder.domain.models.MessageType
import com.example.plantreminder.domain.models.PlantDefinition
import com.example.plantreminder.domain.models.Season
import com.example.plantreminder.domain.models.SeasonalFrequency
import com.example.plantreminder.domain.models.WeatherDay
import com.example.plantreminder.domain.models.WateringRule
import com.example.plantreminder.domain.usecases.GenerateDailyMessageUseCase
import com.example.plantreminder.domain.usecases.GeneratePendingMessagesUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class GeneratePendingMessagesUseCaseTest {
    @Test
    fun clearsExistingDatesAndRebuildsOnlyLastWeekRange() = runTest {
        val existingDate = LocalDate.parse("2026-05-06")
        val preservedDate = LocalDate.parse("2026-04-25")
        val repository = FakePlantDefinitionRepository(
            plants = listOf(
                simplePlant("Romero", LocalDate.parse("2026-04-20")),
            ),
        )
        val conversationRepository = FakeConversationRepository(
            existingMessages = mutableListOf(
                conversationMessage(preservedDate, "Mensaje antiguo"),
                conversationMessage(existingDate, "Ya generado"),
            ),
        )
        val dailyUseCase = GenerateDailyMessageUseCase(
            plantDefinitionRepository = repository,
            conversationRepository = conversationRepository,
            weatherDataSource = FakeWeatherRepository(),
            dailyActionCalculator = DailyActionCalculator(),
            messageBuilder = MessageBuilder(),
        )
        val pendingUseCase = GeneratePendingMessagesUseCase(conversationRepository, repository, dailyUseCase)

        val summary = pendingUseCase(startDate = existingDate, endDate = existingDate.plusDays(1))

        assertThat(summary.generatedCount).isEqualTo(2)
        assertThat(summary.skippedCount).isEqualTo(0)
        assertThat(conversationRepository.messages.map { it.date })
            .containsExactly(preservedDate, existingDate, existingDate.plusDays(1))
            .inOrder()
        assertThat(conversationRepository.messages.none { it.messageText == "Ya generado" }).isTrue()
        assertThat(conversationRepository.messages.any { it.messageText == "Mensaje antiguo" }).isTrue()
    }

    @Test
    fun generatesNoActionsForDaysWithoutMatches() = runTest {
        val start = LocalDate.parse("2026-05-06")
        val repository = FakePlantDefinitionRepository(
            plants = listOf(
                simplePlant("Romero", LocalDate.parse("2026-05-01"), wateringFrequency = 10),
            ),
        )
        val conversationRepository = FakeConversationRepository()
        val dailyUseCase = GenerateDailyMessageUseCase(
            plantDefinitionRepository = repository,
            conversationRepository = conversationRepository,
            weatherDataSource = FakeWeatherRepository(),
            dailyActionCalculator = DailyActionCalculator(),
            messageBuilder = MessageBuilder(),
        )
        val pendingUseCase = GeneratePendingMessagesUseCase(conversationRepository, repository, dailyUseCase)

        pendingUseCase(startDate = start, endDate = start.plusDays(1))

        assertThat(conversationRepository.messages).hasSize(2)
        assertThat(conversationRepository.messages.all { it.messageType == MessageType.NO_ACTIONS }).isTrue()
    }

    @Test
    fun preservesChronologicalOrder() = runTest {
        val start = LocalDate.parse("2026-05-06")
        val repository = FakePlantDefinitionRepository(
            plants = listOf(simplePlant("Romero", LocalDate.parse("2026-05-01"), wateringFrequency = 1)),
        )
        val conversationRepository = FakeConversationRepository()
        val dailyUseCase = GenerateDailyMessageUseCase(
            plantDefinitionRepository = repository,
            conversationRepository = conversationRepository,
            weatherDataSource = FakeWeatherRepository(),
            dailyActionCalculator = DailyActionCalculator(),
            messageBuilder = MessageBuilder(),
        )
        val pendingUseCase = GeneratePendingMessagesUseCase(conversationRepository, repository, dailyUseCase)

        pendingUseCase(startDate = start, endDate = start.plusDays(2))

        assertThat(conversationRepository.messages.map { it.date }).containsExactly(
            start,
            start.plusDays(1),
            start.plusDays(2),
        ).inOrder()
    }

    @Test
    fun recalculatesAllFromStartDateAfterClearingExistingHistory() = runTest {
        val start = LocalDate.parse("2026-05-01")
        val repository = FakePlantDefinitionRepository(
            plants = listOf(simplePlant("Romero", LocalDate.parse("2026-04-20"), wateringFrequency = 1)),
        )
        val conversationRepository = FakeConversationRepository(
            existingMessages = mutableListOf(
                conversationMessage(LocalDate.parse("2026-04-28"), "Mantener"),
                conversationMessage(start, "Viejo mensaje"),
            ),
        )
        val dailyUseCase = GenerateDailyMessageUseCase(
            plantDefinitionRepository = repository,
            conversationRepository = conversationRepository,
            weatherDataSource = FakeWeatherRepository(),
            dailyActionCalculator = DailyActionCalculator(),
            messageBuilder = MessageBuilder(),
        )
        val pendingUseCase = GeneratePendingMessagesUseCase(conversationRepository, repository, dailyUseCase)

        val summary = pendingUseCase(startDate = start, endDate = start.plusDays(2))

        assertThat(summary.generatedCount).isEqualTo(3)
        assertThat(summary.skippedCount).isEqualTo(0)
        assertThat(conversationRepository.messages.map { it.date }).containsExactly(
            LocalDate.parse("2026-04-28"),
            start,
            start.plusDays(1),
            start.plusDays(2),
        ).inOrder()
        assertThat(conversationRepository.messages.none { it.messageText == "Viejo mensaje" }).isTrue()
    }
}

private class FakeWeatherRepository(
    private val weatherDays: List<WeatherDay> = emptyList(),
) : WeatherDataSource {
    override fun observeWeather(): Flow<List<WeatherDay>> = MutableStateFlow(weatherDays)

    override suspend fun getWeatherDays(): List<WeatherDay> = weatherDays

    override suspend fun refreshWeather(): WeatherRefreshResult = error("Not needed in tests")
}

private class FakePlantDefinitionRepository(
    private val plants: List<PlantDefinition>,
) : PlantDefinitionDataSource {
    override suspend fun getAllPlants(): List<PlantDefinition> = plants

    override suspend fun getEarliestStartDate(): LocalDate? = plants.minOfOrNull { it.fechaInicio }
}

private class FakeConversationRepository(
    existingMessages: MutableList<ConversationMessage> = mutableListOf(),
) : ConversationDataSource {
    val messages: MutableList<ConversationMessage> = existingMessages
    private val flow = MutableStateFlow(messages.sortedBy { it.date })

    override fun getConversationFlow(): Flow<List<ConversationMessage>> = flow

    override suspend fun appendMessageForDate(
        dailyMessage: DailyMessage,
        source: MessageSource,
        status: MessageStatus,
        createdAt: Long,
    ): AppendResult {
        if (messages.any { it.date == dailyMessage.date }) return AppendResult.SkippedExisting
        val newMessage = ConversationMessage(
            id = messages.size.toLong() + 1,
            date = dailyMessage.date,
            createdAt = createdAt,
            messageText = dailyMessage.text,
            messageType = dailyMessage.messageType,
            status = status,
            source = source,
            rawPayload = dailyMessage.rawPayload,
        )
        messages += newMessage
        messages.sortBy { it.date }
        flow.value = messages.toList()
        return AppendResult.Inserted(newMessage.id)
    }

    override suspend fun hasMessageForDate(date: LocalDate): Boolean = messages.any { it.date == date }
    override suspend fun markError(date: LocalDate) = Unit
    override suspend fun deleteBetween(fromDate: LocalDate, toDate: LocalDate) {
        messages.removeAll { it.date >= fromDate && it.date <= toDate }
        flow.value = messages.toList()
    }
    override suspend fun deleteAll() {
        messages.clear()
        flow.value = emptyList()
    }
}

private fun simplePlant(
    nombre: String,
    start: LocalDate,
    wateringFrequency: Int = 1,
): PlantDefinition = PlantDefinition(
    id = nombre.lowercase(),
    nombre = nombre,
    especie = null,
    activada = true,
    fechaInicio = start,
    fechaFin = null,
    responsable = "L",
    mostrarEnSiembra = false,
    mesesSiembra = emptyList(),
    mesesRecoleccion = emptyList(),
    riego = WateringRule(SeasonalFrequency(mapOf(Season.PRIMAVERA to wateringFrequency)), emptyList()),
    abono = emptyMap(),
    exposicionSolar = null,
    interior = false,
    notas = emptyList(),
    composicionMaceta = null,
    fuenteInformacionUrl = null,
    fuenteSustratoUrl = null,
    plagas = null,
    metadata = emptyMap(),
    rawPayload = "{}",
)

private fun conversationMessage(date: LocalDate, text: String) = ConversationMessage(
    id = 1,
    date = date,
    createdAt = 0L,
    messageText = text,
    messageType = MessageType.ACTIONS,
    status = MessageStatus.GENERATED,
    source = MessageSource.DEBUG,
    rawPayload = null,
)

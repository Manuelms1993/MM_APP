package com.example.mmapp.app2

import com.example.mmapp.app2.data.input.MenuSyncResult
import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.models.MenuCatalog
import com.example.mmapp.app2.domain.models.MenuDay
import com.example.mmapp.app2.domain.models.MenuOption
import com.example.mmapp.app2.domain.models.MenuPlan
import com.example.mmapp.app2.domain.models.MenuWeek
import com.example.mmapp.app2.domain.usecases.CalculateShoppingListUseCase
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CalculateShoppingListUseCaseTest {
    @Test
    fun `lunes calcula hasta el sabado de la misma semana`() = runTest {
        val useCase = CalculateShoppingListUseCase(FakeMenuCatalogDataSource())

        val result = useCase(today = LocalDate.of(2026, 5, 11))

        assertThat(result.toDate).isEqualTo(LocalDate.of(2026, 5, 16))
    }

    @Test
    fun `miercoles calcula hasta el sabado de la semana siguiente`() = runTest {
        val useCase = CalculateShoppingListUseCase(FakeMenuCatalogDataSource())

        val result = useCase(today = LocalDate.of(2026, 5, 13))

        assertThat(result.toDate).isEqualTo(LocalDate.of(2026, 5, 23))
        assertThat(result.items.map { it.ingredient }).contains("ingrediente comida")
        assertThat(result.items.map { it.ingredient }).contains("ingrediente cena")
    }

    @Test
    fun `cada ingrediente incluye las comidas de origen`() = runTest {
        val useCase = CalculateShoppingListUseCase(FakeMenuCatalogDataSource())

        val result = useCase(today = LocalDate.of(2026, 5, 11))
        val item = result.items.first { it.ingredient == "ingrediente comida" }

        assertThat(item.sources).isNotEmpty()
        assertThat(item.sources.first().mealLabel).isEqualTo("Comida")
        assertThat(item.sources.first().optionName).isEqualTo("opcion-2-1")
        assertThat(item.sources.first().date).isEqualTo(LocalDate.of(2026, 5, 11))
    }
}

private class FakeMenuCatalogDataSource : MenuCatalogDataSource {
    override val loadWarningMessage: StateFlow<String?> = MutableStateFlow(null)

    private val catalog = MenuCatalog(
        lunchPlan = createPlan("comidas", "ingrediente comida"),
        dinnerPlan = createPlan("cenas", "ingrediente cena"),
    )

    override suspend fun getCatalog(): MenuCatalog = catalog

    override suspend fun getEarliestStartDate(): LocalDate = catalog.earliestStartDate()

    override suspend fun refreshLocalSource(): MenuCatalog = catalog

    override suspend fun syncRemoteChanges(): MenuSyncResult = error("No usado en test")
}

private fun createPlan(type: String, ingredient: String): MenuPlan = MenuPlan(
    planType = type,
    startDate = LocalDate.of(2026, 5, 4),
    weeks = List(4) { weekIndex ->
        MenuWeek(
            weekNumber = weekIndex + 1,
            days = List(7) { dayIndex ->
                MenuDay(
                    dayLabel = "dia-${dayIndex + 1}",
                    options = listOf(MenuOption("opcion-${weekIndex + 1}-${dayIndex + 1}", listOf(ingredient))),
                )
            },
        )
    },
)

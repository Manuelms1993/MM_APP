package com.example.mmapp.app2.domain.usecases

import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.models.MenuOption
import com.example.mmapp.app2.domain.models.ShoppingList
import com.example.mmapp.app2.domain.models.ShoppingListItem
import com.example.mmapp.app2.domain.models.ShoppingListSource
import java.time.DayOfWeek
import java.time.LocalDate

class CalculateShoppingListUseCase(
    private val menuCatalogDataSource: MenuCatalogDataSource,
) {
    suspend operator fun invoke(
        today: LocalDate = LocalDate.now(),
    ): ShoppingList {
        val catalog = menuCatalogDataSource.getCatalog()
        val targetSaturday = resolveTargetSaturday(today)
        val ingredientCounts = linkedMapOf<String, Int>()
        val ingredientSources = linkedMapOf<String, MutableList<ShoppingListSource>>()

        generateSequence(today) { current ->
            current.plusDays(1).takeUnless { it.isAfter(targetSaturday) }
        }.forEach { date ->
            val selection = catalog.selectionForDate(date)
            accumulateIngredients(
                date = date,
                mealLabel = "Comida",
                options = selection.lunchOptions,
                ingredientCounts = ingredientCounts,
                ingredientSources = ingredientSources,
            )
            accumulateIngredients(
                date = date,
                mealLabel = "Cena",
                options = selection.dinnerOptions,
                ingredientCounts = ingredientCounts,
                ingredientSources = ingredientSources,
            )
        }

        return ShoppingList(
            fromDate = today,
            toDate = targetSaturday,
            items = ingredientCounts.entries
                .sortedBy { it.key.lowercase() }
                .map { entry ->
                    ShoppingListItem(
                        ingredient = entry.key,
                        occurrences = entry.value,
                        sources = ingredientSources[entry.key].orEmpty(),
                    )
                },
        )
    }

    private fun accumulateIngredients(
        date: LocalDate,
        mealLabel: String,
        options: List<MenuOption>,
        ingredientCounts: MutableMap<String, Int>,
        ingredientSources: MutableMap<String, MutableList<ShoppingListSource>>,
    ) {
        options.forEach { option ->
            option.ingredients.forEach { ingredient ->
                val key = ingredient.trim()
                if (key.isBlank()) return@forEach

                ingredientCounts[key] = (ingredientCounts[key] ?: 0) + 1
                ingredientSources.getOrPut(key) { mutableListOf() }
                    .add(
                        ShoppingListSource(
                            date = date,
                            mealLabel = mealLabel,
                            optionName = option.name,
                        ),
                    )
            }
        }
    }

    internal fun resolveTargetSaturday(today: LocalDate): LocalDate {
        val saturdayThisWeek = today.with(DayOfWeek.SATURDAY)
        return if (today.dayOfWeek.value >= DayOfWeek.WEDNESDAY.value) {
            saturdayThisWeek.plusWeeks(1)
        } else {
            saturdayThisWeek
        }
    }
}

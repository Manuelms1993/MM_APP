package com.example.mmapp.app2.domain.models

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class MenuOption(
    val name: String,
    val ingredients: List<String>,
)

data class MenuDay(
    val dayLabel: String,
    val options: List<MenuOption>,
)

data class MenuWeek(
    val weekNumber: Int,
    val days: List<MenuDay>,
)

data class MenuPlan(
    val planType: String,
    val startDate: LocalDate,
    val weeks: List<MenuWeek>,
) {
    private val flattenedDays: List<MenuDay> by lazy { weeks.flatMap { it.days } }

    fun optionsForDate(date: LocalDate): List<MenuOption> {
        if (flattenedDays.isEmpty()) return emptyList()
        val cycleLength = flattenedDays.size
        val totalDays = ChronoUnit.DAYS.between(startDate, date).toInt()
        val dayOffset = Math.floorMod(totalDays, cycleLength)
        return flattenedDays[dayOffset].options
    }
}

data class MenuCatalog(
    val lunchPlan: MenuPlan,
    val dinnerPlan: MenuPlan,
) {
    fun earliestStartDate(): LocalDate = minOf(lunchPlan.startDate, dinnerPlan.startDate)

    fun selectionForDate(date: LocalDate): MenuDaySelection = MenuDaySelection(
        date = date,
        lunchOptions = lunchPlan.optionsForDate(date),
        dinnerOptions = dinnerPlan.optionsForDate(date),
    )
}

data class MenuDaySelection(
    val date: LocalDate,
    val lunchOptions: List<MenuOption>,
    val dinnerOptions: List<MenuOption>,
)

data class ShoppingListItem(
    val ingredient: String,
    val occurrences: Int,
    val sources: List<ShoppingListSource>,
)

data class ShoppingListSource(
    val date: LocalDate,
    val mealLabel: String,
    val optionName: String,
)

data class ShoppingList(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val items: List<ShoppingListItem>,
)

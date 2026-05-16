package com.example.mmapp.app2.data.input.validation

import com.example.mmapp.app2.domain.models.MenuPlan

class MenuPlanValidator {
    fun validate(plan: MenuPlan, fileName: String): List<String> {
        val warnings = mutableListOf<String>()
        if (plan.weeks.size != 4) {
            warnings += "$fileName debería tener 4 semanas."
        }
        plan.weeks.forEach { week ->
            if (week.days.size != 7) {
                warnings += "$fileName semana ${week.weekNumber} debería tener 7 días."
            }
            week.days.forEachIndexed { index, day ->
                if (day.options.isEmpty()) {
                    warnings += "$fileName semana ${week.weekNumber} día ${index + 1} no tiene opciones."
                }
                day.options.forEach { option ->
                    if (option.name.isBlank()) warnings += "$fileName contiene una opción sin nombre."
                    if (option.ingredients.isEmpty()) warnings += "$fileName opción '${option.name}' sin ingredientes."
                }
            }
        }
        return warnings
    }
}

package com.example.mmapp.app2.data.input.validation

import com.example.mmapp.app2.domain.models.MenuPlan

data class MenuPlanFactoryResult(
    val menuPlan: MenuPlan?,
    val warnings: List<String>,
)

interface MenuPlanFactory {
    fun create(rawJson: String, sourceName: String): MenuPlanFactoryResult
}

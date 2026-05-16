package com.example.mmapp.app2.data.input.validation

import com.example.mmapp.app2.data.input.RawMenuInput
import com.example.mmapp.app2.domain.models.MenuCatalog

data class MenuCatalogValidationReport(
    val catalog: MenuCatalog?,
    val warnings: List<String>,
)

class MenuCatalogValidator(
    private val menuPlanFactory: MenuPlanFactory,
    private val menuPlanValidator: MenuPlanValidator,
) {
    fun validate(inputs: List<RawMenuInput>): MenuCatalogValidationReport {
        val warnings = mutableListOf<String>()
        val plansByFileName = inputs.associate { input ->
            val result = menuPlanFactory.create(input.rawJson, input.fileName)
            warnings += result.warnings
            val plan = result.menuPlan?.also { warnings += menuPlanValidator.validate(it, input.fileName) }
            input.fileName to plan
        }

        val lunchPlan = plansByFileName["comidas.json"]
        val dinnerPlan = plansByFileName["cenas.json"]
        if (lunchPlan == null) warnings += "Falta comidas.json o no es válido."
        if (dinnerPlan == null) warnings += "Falta cenas.json o no es válido."

        return MenuCatalogValidationReport(
            catalog = if (lunchPlan != null && dinnerPlan != null) MenuCatalog(lunchPlan, dinnerPlan) else null,
            warnings = warnings,
        )
    }
}

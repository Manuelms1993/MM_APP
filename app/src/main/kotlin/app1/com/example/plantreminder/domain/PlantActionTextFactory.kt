package com.example.mmapp.app1.domain

import com.example.mmapp.app1.domain.models.FertilizerDoseMode
import com.example.mmapp.app1.domain.models.FertilizerRule

class PlantActionTextFactory {
    fun wateringTitle(): String = "regar"

    fun fertilizerTitle(rule: FertilizerRule): String = "aplicar ${rule.tipo.toReadableLabel()}"

    fun pestInspectionTitle(): String = "revisar plagas"

    fun pestInspectionDetails(rule: com.example.mmapp.app1.domain.models.PestMonitoringRule): List<String> = buildList {
        if (rule.commonIssues.isNotEmpty()) {
            add("Vigilar: ${rule.commonIssues.joinToString(", ") { it.toReadableLabel() }}")
        }
        addAll(rule.notes)
    }

    fun fertilizerDetails(rule: FertilizerRule): List<String> = buildList {
        rule.cantidad?.takeIf { it.isNotBlank() }?.let { add("cantidad: $it") }
        when (rule.dosis) {
            FertilizerDoseMode.FABRICANTE_DIVIDIDO_2 -> add("media dosis")
            FertilizerDoseMode.PERSONALIZADO -> rule.dosisTextoOriginal?.let { add("dosis: ${it.toReadableLabel()}") }
            FertilizerDoseMode.NORMAL -> Unit
        }
        rule.recordatorio?.takeIf { it.isNotBlank() }?.let { add(it) }
    }

    private fun String.toReadableLabel(): String = replace('_', ' ')
        .replace("humus lombriz", "humus de lombriz")
}

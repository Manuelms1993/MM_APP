package com.example.plantreminder.ui

import com.example.plantreminder.data.input.PlantSyncResult
import com.example.plantreminder.domain.usecases.PendingGenerationSummary

class HomeOperationMessageFormatter {
    fun format(summary: PendingGenerationSummary): String = buildString {
        append("Generados: ${summary.generatedCount}")
        append(" · Omitidos: ${summary.skippedCount}")
        if (summary.errors.isNotEmpty()) {
            append(" · Errores: ${summary.errors.size}")
        }
    }

    fun format(result: PlantSyncResult): String = buildString {
        append("Sincronizado")
        append(" · Nuevos: ${result.newCount}")
        append(" · Actualizados: ${result.updatedCount}")
        append(" · Sin cambios: ${result.unchangedCount}")
        if (result.removedCount > 0) {
            append(" · Eliminados: ${result.removedCount}")
        }
    }
}

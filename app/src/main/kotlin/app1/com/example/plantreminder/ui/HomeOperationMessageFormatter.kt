package com.example.mmapp.app1.ui

import com.example.mmapp.app1.data.input.PlantSyncResult
import com.example.mmapp.app1.domain.usecases.PendingGenerationSummary
import com.example.mmapp.remote.RemoteSyncErrorFormatter

class HomeOperationMessageFormatter {
    private val remoteSyncErrorFormatter = RemoteSyncErrorFormatter()

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

    fun formatSyncError(throwable: Throwable): String = remoteSyncErrorFormatter.format(throwable)
}

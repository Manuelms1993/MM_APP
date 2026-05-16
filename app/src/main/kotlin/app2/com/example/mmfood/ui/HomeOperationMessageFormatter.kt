package com.example.mmapp.app2.ui

import com.example.mmapp.app2.data.input.MenuSyncResult
import com.example.mmapp.app2.domain.usecases.PendingGenerationSummary
import com.example.mmapp.remote.RemoteSyncErrorFormatter

class HomeOperationMessageFormatter {
    private val remoteSyncErrorFormatter = RemoteSyncErrorFormatter()

    fun format(summary: PendingGenerationSummary): String = buildString {
        append("Generados: ${summary.generatedCount}")
        if (summary.errors.isNotEmpty()) {
            append(" · Errores: ${summary.errors.size}")
        }
    }

    fun format(result: MenuSyncResult): String = buildString {
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

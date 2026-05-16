package com.example.mmapp.app2.ui.usecases

import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.ui.HomeOperationMessageFormatter

class SyncMenuInputsUseCase(
    private val menuCatalogDataSource: MenuCatalogDataSource,
    private val messageFormatter: HomeOperationMessageFormatter,
) {
    suspend operator fun invoke(): String = runCatching {
        val result = menuCatalogDataSource.syncRemoteChanges()
        messageFormatter.format(result)
    }.getOrElse(messageFormatter::formatSyncError)
}

package com.example.mmapp.app2.data.repositories

import com.example.mmapp.app2.data.input.MenuSyncResult
import com.example.mmapp.app2.domain.models.MenuCatalog
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow

interface MenuCatalogDataSource {
    val loadWarningMessage: StateFlow<String?>

    suspend fun getCatalog(): MenuCatalog

    suspend fun getEarliestStartDate(): LocalDate?

    suspend fun refreshLocalSource(): MenuCatalog

    suspend fun syncRemoteChanges(): MenuSyncResult
}

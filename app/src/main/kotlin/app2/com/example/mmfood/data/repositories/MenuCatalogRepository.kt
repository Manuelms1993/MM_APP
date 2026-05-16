package com.example.mmapp.app2.data.repositories

import android.util.Log
import com.example.mmapp.app2.data.input.RawMenuLoadResult
import com.example.mmapp.app2.data.input.source.MenuInputLoader
import com.example.mmapp.app2.data.input.source.MenuInputSyncService
import com.example.mmapp.app2.data.input.validation.MenuCatalogValidator
import com.example.mmapp.app2.data.input.MenuSyncResult
import com.example.mmapp.app2.domain.models.MenuCatalog
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MenuCatalogRepository(
    private val menuInputLoader: MenuInputLoader,
    private val menuInputSyncService: MenuInputSyncService,
    private val menuCatalogValidator: MenuCatalogValidator,
) : MenuCatalogDataSource {
    private val cacheMutex = Mutex()

    @Volatile
    private var cachedCatalog: MenuCatalog? = null

    private val _loadWarningMessage = MutableStateFlow<String?>(null)
    override val loadWarningMessage: StateFlow<String?> = _loadWarningMessage.asStateFlow()

    override suspend fun getCatalog(): MenuCatalog = withContext(Dispatchers.IO) {
        cachedCatalog ?: cacheMutex.withLock {
            cachedCatalog ?: loadCatalog(menuInputLoader.loadMenuJsonFiles())
        }
    }

    override suspend fun getEarliestStartDate(): LocalDate? = getCatalog().earliestStartDate()

    override suspend fun refreshLocalSource(): MenuCatalog = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            cachedCatalog = null
        }
        getCatalog()
    }

    override suspend fun syncRemoteChanges(): MenuSyncResult = withContext(Dispatchers.IO) {
        val result = menuInputSyncService.syncFromGithub()
        cacheMutex.withLock {
            cachedCatalog = null
        }
        result
    }

    private fun loadCatalog(loadResult: RawMenuLoadResult): MenuCatalog {
        _loadWarningMessage.value = loadResult.warningMessage
        val validation = menuCatalogValidator.validate(loadResult.inputs)
        validation.warnings.forEach { warning -> Log.w(TAG, warning) }
        return requireNotNull(validation.catalog) { "No se pudo construir el catálogo de menús." }
            .also { cachedCatalog = it }
    }

    companion object {
        private const val TAG = "MenuCatalogRepo"
    }
}

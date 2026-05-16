package com.example.mmapp.app2.ui.usecases

import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import kotlinx.coroutines.flow.StateFlow

class ObserveMenuLoadWarningsUseCase(
    private val menuCatalogDataSource: MenuCatalogDataSource,
) {
    operator fun invoke(): StateFlow<String?> = menuCatalogDataSource.loadWarningMessage
}

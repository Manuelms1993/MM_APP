package com.example.mmapp.app2.ui.usecases

import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.models.MenuCatalog

class LoadMenuCatalogUseCase(
    private val menuCatalogDataSource: MenuCatalogDataSource,
) {
    suspend operator fun invoke(): MenuCatalog = menuCatalogDataSource.getCatalog()
}

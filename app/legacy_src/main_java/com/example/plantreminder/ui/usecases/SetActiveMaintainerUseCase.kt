package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.AppPreferencesDataSource

class SetActiveMaintainerUseCase(
    private val appPreferencesDataSource: AppPreferencesDataSource,
) {
    suspend operator fun invoke(maintainer: String) {
        appPreferencesDataSource.setActiveMaintainer(maintainer)
    }
}

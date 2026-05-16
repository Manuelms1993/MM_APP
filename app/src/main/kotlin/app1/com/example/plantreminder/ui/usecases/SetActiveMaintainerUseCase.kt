package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.AppPreferencesDataSource

class SetActiveMaintainerUseCase(
    private val appPreferencesDataSource: AppPreferencesDataSource,
) {
    suspend operator fun invoke(maintainer: String) {
        appPreferencesDataSource.setActiveMaintainer(maintainer)
    }
}

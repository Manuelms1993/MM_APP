package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.AppPreferencesDataSource

class ObserveActiveMaintainerUseCase(
    private val appPreferencesDataSource: AppPreferencesDataSource,
) {
    operator fun invoke() = appPreferencesDataSource.observeActiveMaintainer()
}

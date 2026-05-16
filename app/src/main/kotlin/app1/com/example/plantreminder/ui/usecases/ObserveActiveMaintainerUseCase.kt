package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.AppPreferencesDataSource

class ObserveActiveMaintainerUseCase(
    private val appPreferencesDataSource: AppPreferencesDataSource,
) {
    operator fun invoke() = appPreferencesDataSource.observeActiveMaintainer()
}

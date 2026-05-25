package com.example.mmapp.settings.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mmapp.settings.AppContainer
import com.example.mmapp.settings.ui.screens.SettingsScreen

@Composable
fun SettingsApp(
    container: AppContainer,
    viewModelKey: String,
    onNotificationSettingsChanged: () -> Unit,
    onProcessSettingsChanged: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(
        key = viewModelKey,
        factory = SettingsViewModel.factory(container),
    )
    SettingsScreen(
        viewModel = viewModel,
        onNotificationSettingsChanged = onNotificationSettingsChanged,
        onProcessSettingsChanged = onProcessSettingsChanged,
    )
}

package com.example.mmapp.app1.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mmapp.app1.AppContainer
import com.example.mmapp.app1.ui.screens.HomeScreen
import com.example.mmapp.app1.ui.theme.PlantReminderTheme

@Composable
fun PlantReminderApp(
    container: AppContainer,
    viewModelKey: String,
) {
    PlantReminderTheme {
        val viewModel: HomeViewModel = viewModel(
            key = viewModelKey,
            factory = HomeViewModel.factory(container),
        )
        HomeScreen(viewModel = viewModel)
    }
}

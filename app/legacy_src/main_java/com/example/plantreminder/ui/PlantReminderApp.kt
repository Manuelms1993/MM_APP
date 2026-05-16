package com.example.plantreminder.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plantreminder.AppContainer
import com.example.plantreminder.ui.screens.HomeScreen
import com.example.plantreminder.ui.theme.PlantReminderTheme

@Composable
fun PlantReminderApp(
    container: AppContainer,
) {
    PlantReminderTheme {
        val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
        HomeScreen(viewModel = viewModel)
    }
}


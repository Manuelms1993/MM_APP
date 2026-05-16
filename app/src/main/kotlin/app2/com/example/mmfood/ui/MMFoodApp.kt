package com.example.mmapp.app2.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mmapp.app2.AppContainer
import com.example.mmapp.app2.ui.screens.HomeScreen
import com.example.mmapp.app2.ui.theme.MMFoodTheme

@Composable
fun MMFoodApp(
    container: AppContainer,
    viewModelKey: String,
) {
    MMFoodTheme {
        val viewModel: HomeViewModel = viewModel(
            key = viewModelKey,
            factory = HomeViewModel.factory(container),
        )
        HomeScreen(viewModel = viewModel)
    }
}

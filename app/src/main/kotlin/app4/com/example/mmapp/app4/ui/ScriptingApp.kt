package com.example.mmapp.app4.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mmapp.app4.AppContainer
import com.example.mmapp.app4.ui.screens.HomeScreen
import com.example.mmapp.app4.ui.theme.ScriptingTheme

@Composable
fun ScriptingApp(
    container: AppContainer,
    viewModelKey: String,
) {
    ScriptingTheme {
        val viewModel: HomeViewModel = viewModel(
            key = viewModelKey,
            factory = HomeViewModel.factory(container),
        )
        HomeScreen(viewModel = viewModel)
    }
}

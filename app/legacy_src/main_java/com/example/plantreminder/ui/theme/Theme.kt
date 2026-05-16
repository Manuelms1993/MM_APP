package com.example.plantreminder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LeafGreen,
    secondary = SoilBrown,
    tertiary = SoftGreen,
    background = Mist,
)

private val DarkColors = darkColorScheme(
    primary = SoftGreen,
    secondary = SoilBrown,
)

@Composable
fun PlantReminderTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}

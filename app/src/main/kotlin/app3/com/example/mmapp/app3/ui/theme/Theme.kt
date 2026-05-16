package com.example.mmapp.app3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LeafGreen,
    secondary = SoilBrown,
    tertiary = SoftGreen,
    background = Mist,
)

@Composable
fun TravelGuideTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}

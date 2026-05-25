package com.example.mmapp.app4.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ScriptBlue,
    secondary = ScriptSlate,
    tertiary = ScriptIce,
    background = ScriptMist,
)

@Composable
fun ScriptingTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}

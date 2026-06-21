package com.example.mmapp.settings.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mmapp.settings.data.repositories.AppSettingsRepository
import com.example.mmapp.settings.ui.NotificationSettingsUiState
import com.example.mmapp.settings.ui.ProcessSettingsUiState
import com.example.mmapp.settings.ui.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNotificationSettingsChanged: () -> Unit,
    onProcessSettingsChanged: () -> Unit,
) {
    val settings by viewModel.appNotificationSettings.collectAsStateWithLifecycle()
    val processSettings by viewModel.processSettings.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Notificaciones", "Procesos").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> NotificationsTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                settings = settings,
                onSaveSettings = { draftSettings ->
                    viewModel.saveNotificationSettings(draftSettings, onSaved = onNotificationSettingsChanged)
                },
            )

            else -> ProcessesTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                processSettings = processSettings,
                onSaveSettings = { draftSettings ->
                    viewModel.saveProcessSettings(draftSettings, onSaved = onProcessSettingsChanged)
                },
            )
        }
    }
}

@Composable
private fun NotificationsTab(
    modifier: Modifier,
    settings: List<NotificationSettingsUiState>,
    onSaveSettings: (List<NotificationSettingsUiState>) -> Unit,
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    var draftSettings by remember { mutableStateOf(settings) }
    var hasPendingChanges by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        if (!hasPendingChanges) {
            draftSettings = settings
        }
    }

    fun updateDraft(appId: String, transform: (NotificationSettingsUiState) -> NotificationSettingsUiState) {
        draftSettings = draftSettings.map { notification ->
            if (notification.appId == appId) transform(notification) else notification
        }
        hasPendingChanges = true
    }

    val plantNotification = draftSettings.firstOrNull { it.appId == AppSettingsRepository.PLANTS_NOTIFICATION_ID }
    val foodNotifications = draftSettings.filter {
        it.appId == AppSettingsRepository.FOOD_LUNCH_NOTIFICATION_ID || it.appId == AppSettingsRepository.FOOD_DINNER_NOTIFICATION_ID
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SaveSettingsRow(
                hasPendingChanges = hasPendingChanges,
                onSave = {
                    onSaveSettings(draftSettings)
                    hasPendingChanges = false
                },
            )
        }
        item {
            plantNotification?.let { notification ->
                ExpandableSettingsBlock(
                    title = notification.title,
                    expanded = expandedStates[notification.appId] == true,
                    onExpandedChange = { expandedStates[notification.appId] = it },
                ) {
                    NotificationDetails(
                        notification = notification,
                        onNotificationEnabledChanged = { appId, enabled ->
                            updateDraft(appId) { it.copy(enabled = enabled) }
                        },
                        onIntervalDaysChanged = { appId, intervalDays ->
                            updateDraft(appId) { it.copy(intervalDays = intervalDays) }
                        },
                        onHourOfDayChanged = { appId, hourOfDay ->
                            updateDraft(appId) { it.copy(hourOfDay = hourOfDay) }
                        },
                    )
                }
            }
        }
        item {
            val groupExpanded = expandedStates[FOOD_GROUP_ID] == true
            ExpandableSettingsBlock(
                title = "Comidas",
                expanded = groupExpanded,
                onExpandedChange = { expandedStates[FOOD_GROUP_ID] = it },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    foodNotifications.forEach { notification ->
                        ExpandableSubBlock(
                            title = notification.title,
                            expanded = expandedStates[notification.appId] == true,
                            onExpandedChange = { expandedStates[notification.appId] = it },
                        ) {
                            NotificationDetails(
                                notification = notification,
                                onNotificationEnabledChanged = { appId, enabled ->
                                    updateDraft(appId) { it.copy(enabled = enabled) }
                                },
                                onIntervalDaysChanged = { appId, intervalDays ->
                                    updateDraft(appId) { it.copy(intervalDays = intervalDays) }
                                },
                                onHourOfDayChanged = { appId, hourOfDay ->
                                    updateDraft(appId) { it.copy(hourOfDay = hourOfDay) }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessesTab(
    modifier: Modifier,
    processSettings: List<ProcessSettingsUiState>,
    onSaveSettings: (List<ProcessSettingsUiState>) -> Unit,
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    var draftSettings by remember { mutableStateOf(processSettings) }
    var hasPendingChanges by remember { mutableStateOf(false) }

    LaunchedEffect(processSettings) {
        if (!hasPendingChanges) {
            draftSettings = processSettings
        }
    }

    fun updateDraft(processId: String, transform: (ProcessSettingsUiState) -> ProcessSettingsUiState) {
        draftSettings = draftSettings.map { process ->
            if (process.processId == processId) transform(process) else process
        }
        hasPendingChanges = true
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SaveSettingsRow(
                hasPendingChanges = hasPendingChanges,
                onSave = {
                    onSaveSettings(draftSettings)
                    hasPendingChanges = false
                },
            )
        }
        items(draftSettings.size) { index ->
            val process = draftSettings[index]
            ExpandableSettingsBlock(
                title = process.title,
                expanded = expandedStates[process.processId] == true,
                onExpandedChange = { expandedStates[process.processId] = it },
            ) {
                ProcessEnabledRow(
                    enabled = process.enabled,
                    onCheckedChange = { enabled ->
                        updateDraft(process.processId) { it.copy(enabled = enabled) }
                    },
                )
                NumberSettingField(
                    label = "Cada cuántos días",
                    value = process.intervalDays,
                    onValueChanged = { intervalDays ->
                        updateDraft(process.processId) { it.copy(intervalDays = intervalDays) }
                    },
                    range = 1..30,
                )
                NumberSettingField(
                    label = "Hora del día (0-23)",
                    value = process.hourOfDay,
                    onValueChanged = { hourOfDay ->
                        updateDraft(process.processId) { it.copy(hourOfDay = hourOfDay) }
                    },
                    range = 0..23,
                )
            }
        }
    }
}

@Composable
private fun NotificationDetails(
    notification: NotificationSettingsUiState,
    onNotificationEnabledChanged: (String, Boolean) -> Unit,
    onIntervalDaysChanged: (String, Int) -> Unit,
    onHourOfDayChanged: (String, Int) -> Unit,
) {
    ProcessEnabledRow(
        enabled = notification.enabled,
        onCheckedChange = { onNotificationEnabledChanged(notification.appId, it) },
    )
    NumberSettingField(
        label = "Cada cuántos días",
        value = notification.intervalDays,
        onValueChanged = { onIntervalDaysChanged(notification.appId, it) },
        range = 1..30,
    )
    NumberSettingField(
        label = "Hora del día (0-23)",
        value = notification.hourOfDay,
        onValueChanged = { onHourOfDayChanged(notification.appId, it) },
        range = 0..23,
    )
}

@Composable
private fun SaveSettingsRow(
    hasPendingChanges: Boolean,
    onSave: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (hasPendingChanges) "Cambios pendientes" else "Configuración guardada",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasPendingChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onSave,
                enabled = hasPendingChanges,
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun ExpandableSettingsBlock(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                ExpandableHeader(
                    title = title,
                    expanded = expanded,
                    modifier = Modifier.clickable { onExpandedChange(!expanded) },
                )
                if (expanded) {
                    content()
                }
            },
        )
    }
}

@Composable
private fun ExpandableSubBlock(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ExpandableHeader(
                title = title,
                expanded = expanded,
                modifier = Modifier.clickable { onExpandedChange(!expanded) },
            )
            if (expanded) {
                content()
            }
        }
    }
}

@Composable
private fun ExpandableHeader(
    title: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Contraer" else "Expandir",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ProcessEnabledRow(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NotificationStateLabel(
            label = "No",
            selected = !enabled,
            onClick = { onCheckedChange(false) },
        )
        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange,
        )
        NotificationStateLabel(
            label = "Sí",
            selected = enabled,
            onClick = { onCheckedChange(true) },
        )
    }
}

@Composable
private fun NumberSettingField(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    range: IntRange,
) {
    var rawValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = rawValue,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            rawValue = digitsOnly
            digitsOnly.toIntOrNull()?.coerceIn(range.first, range.last)?.let(onValueChanged)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        supportingText = { Text("Rango permitido: ${range.first}-${range.last}") },
    )
}

@Composable
private fun NotificationStateLabel(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private const val FOOD_GROUP_ID = "food_group"

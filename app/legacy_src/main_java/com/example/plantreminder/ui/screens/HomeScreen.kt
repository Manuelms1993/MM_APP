package com.example.plantreminder.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plantreminder.domain.models.ConversationMessage
import com.example.plantreminder.domain.models.PlantDefinition
import com.example.plantreminder.domain.models.WeatherDay
import com.example.plantreminder.ui.HomeViewModel
import com.example.plantreminder.ui.PlantDisplayFormatter
import com.example.plantreminder.ui.components.DaySection
import com.example.plantreminder.ui.components.messageMatchesMaintainer
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

private const val HistoryDayLimit = 30

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val displayFormatter = remember { PlantDisplayFormatter() }
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val showFullHistory by viewModel.showFullHistory.collectAsStateWithLifecycle()
    val warningMessage by viewModel.warningMessage.collectAsStateWithLifecycle()
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val weatherStatusMessage by viewModel.weatherStatusMessage.collectAsStateWithLifecycle()
    val isWeatherRefreshing by viewModel.isWeatherRefreshing.collectAsStateWithLifecycle()
    val activeMaintainer by viewModel.activeMaintainer.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    val maintainerConversation = remember(conversation, activeMaintainer) {
        conversation.filter { messageMatchesMaintainer(it, activeMaintainer) }
    }
    val visiblePlants = remember(plants, activeMaintainer) {
        plants.filter { it.activada && it.responsable.uppercase() == activeMaintainer }
    }
    val visibleConversation = remember(maintainerConversation, showFullHistory) {
        filterConversation(maintainerConversation, showFullHistory)
    }
    val groupedConversation = remember(visibleConversation) {
        visibleConversation.groupBy { it.date }.toSortedMap()
    }
    val hasHiddenHistory = remember(maintainerConversation, groupedConversation) {
        maintainerConversation.groupBy { it.date }.size > groupedConversation.size
    }

    LaunchedEffect(selectedTab, groupedConversation.size) {
        if (selectedTab == 0 && groupedConversation.isNotEmpty()) {
            listState.animateScrollToItem(groupedConversation.size - 1)
        }
    }

    LaunchedEffect(warningMessage) {
        val message = warningMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissWarning()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Cuidados", "Plantación", "Info", "Tiempo").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = if (title == "Plantación") {
                                    MaterialTheme.typography.labelMedium
                                } else {
                                    MaterialTheme.typography.labelLarge
                                },
                            )
                        },
                    )
                }
            }
        },
        bottomBar = {
            if (selectedTab == 0) {
                Surface(shadowElevation = 6.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = viewModel::generatePending,
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                if (isBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Actualizar")
                                }
                            }
                            OutlinedButton(
                                onClick = viewModel::syncRemoteInputs,
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Sincronizar")
                            }
                        }
                        MaintainerSwitch(
                            activeMaintainer = activeMaintainer,
                            onMaintainerChange = viewModel::setActiveMaintainer,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> CareTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                groupedConversation = groupedConversation,
                listState = listState,
                hasHiddenHistory = hasHiddenHistory,
                onShowFullHistory = viewModel::showFullHistory,
                statusMessage = statusMessage,
                activeMaintainer = activeMaintainer,
            )

            1 -> PlantingTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                plants = visiblePlants,
                displayFormatter = displayFormatter,
                statusMessage = statusMessage,
            )

            2 -> InfoTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                plants = visiblePlants,
                displayFormatter = displayFormatter,
            )

            else -> WeatherTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                weather = weather,
                isRefreshing = isWeatherRefreshing,
                statusMessage = weatherStatusMessage,
                onRefresh = viewModel::refreshWeather,
            )
        }
    }
}

@Composable
private fun MaintainerSwitch(
    activeMaintainer: String,
    onMaintainerChange: (String) -> Unit,
) {
    val isRightSelected = activeMaintainer == "R"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        MaintainerLabel(
            label = "L",
            selected = !isRightSelected,
            onClick = { onMaintainerChange("L") },
        )
        Switch(
            checked = isRightSelected,
            onCheckedChange = { checked ->
                onMaintainerChange(if (checked) "R" else "L")
            },
        )
        MaintainerLabel(
            label = "R",
            selected = isRightSelected,
            onClick = { onMaintainerChange("R") },
        )
    }
}

@Composable
private fun MaintainerLabel(
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

@Composable
private fun CareTab(
    modifier: Modifier,
    groupedConversation: Map<LocalDate, List<ConversationMessage>>,
    listState: LazyListState,
    hasHiddenHistory: Boolean,
    onShowFullHistory: () -> Unit,
    statusMessage: String?,
    activeMaintainer: String,
) {
    if (groupedConversation.isEmpty()) {
        EmptyState(
            modifier = modifier,
            statusMessage = statusMessage,
        )
        return
    }

    val showJumpToToday by remember {
        derivedStateOf {
            val visibleLastIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && visibleLastIndex < totalItems - 1
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            groupedConversation.forEach { (date, messages) ->
                item(key = date.toString()) {
                    DaySection(
                        date = date,
                        messages = messages,
                        activeMaintainer = activeMaintainer,
                    )
                }
            }

            if (hasHiddenHistory) {
                item("show_history") {
                    TextButton(
                        onClick = onShowFullHistory,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Mostrar todo el histórico")
                    }
                }
            }

            statusMessage?.let { status ->
                item(key = "status") {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (showJumpToToday) {
            Button(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                    }
                },
            ) {
                Text(
                    text = "Hoy",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PlantingTab(
    modifier: Modifier,
    plants: List<PlantDefinition>,
    displayFormatter: PlantDisplayFormatter,
    statusMessage: String?,
) {
    val currentMonth = LocalDate.now().monthValue
    val monthName = Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    val sowingPlants = plants
        .filter { it.mostrarEnSiembra && currentMonth in it.mesesSiembra }
        .sortedBy { it.nombre.lowercase() }
    val harvestPlants = plants.filter { currentMonth in it.mesesRecoleccion }.sortedBy { it.nombre.lowercase() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item("month") {
            Text(
                text = "Mes actual: $monthName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item("sowing") {
            PlantMonthSection(
                title = "Sembrar este mes",
                plants = sowingPlants,
                emptyText = "No hay plantas para sembrar este mes.",
            )
        }
        item("harvest") {
            PlantMonthSection(
                title = "Recoger este mes",
                plants = harvestPlants,
                emptyText = "No hay plantas para recoger este mes.",
            )
        }
        statusMessage?.let { status ->
            item("status") {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PlantMonthSection(
    title: String,
    plants: List<PlantDefinition>,
    emptyText: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (plants.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            plants.forEach { plant ->
                Text(
                    text = "• ${plant.nombre}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun InfoTab(
    modifier: Modifier,
    plants: List<PlantDefinition>,
    displayFormatter: PlantDisplayFormatter,
) {
    val context = LocalContext.current
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    val sortedPlants = remember(plants) { plants.sortedBy { it.nombre.lowercase() } }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        sortedPlants.forEach { plant ->
            item(plant.id) {
                val isExpanded = expandedStates[plant.id] ?: false
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedStates[plant.id] = !isExpanded }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = plant.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (isExpanded) "Ocultar" else "Ver",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        if (isExpanded) {
                            PlantInfoLine("Especie", plant.especie ?: "-")
                            PlantInfoLine("Luz", displayFormatter.readableValue(plant.exposicionSolar))
                            PlantInfoLine("Ubicación", if (plant.interior) "Interior" else "Exterior")
                            PlantInfoLine("Maceta", displayFormatter.readableValue(plant.metadata["tamanoMaceta"]))
                            PlantInfoLine("Sembrable", if (plant.mostrarEnSiembra) "Sí" else "No")
                            PlantInfoMultilineSection(
                                label = "Composición de maceta",
                                lines = displayFormatter.pottingMixLines(plant.composicionMaceta),
                            )
                            PlantInfoMultilineSection(
                                label = "Riego",
                                lines = displayFormatter.wateringLines(plant.riego),
                            )
                            PlantInfoMultilineSection(
                                label = "Abono",
                                lines = displayFormatter.fertilizerLines(plant.abono),
                            )
                            PlantInfoMultilineSection(
                                label = "Plagas",
                                lines = displayFormatter.pestMonitoringLines(plant.plagas),
                            )
                            PlantInfoLine("Prioridad", displayFormatter.readableValue(plant.metadata["prioridad"]))
                            PlantInfoLine("Siembra", displayFormatter.monthSummary(plant.mesesSiembra))
                            PlantInfoLine("Recolección", displayFormatter.monthSummary(plant.mesesRecoleccion))
                            PlantInfoUrlLine(
                                label = "Fuente de información",
                                url = plant.fuenteInformacionUrl,
                                onOpenUrl = { url -> context.openUrl(url) },
                            )
                            PlantInfoUrlLine(
                                label = "Fuente del sustrato",
                                url = plant.fuenteSustratoUrl,
                                onOpenUrl = { url -> context.openUrl(url) },
                            )
                            if (plant.notas.isNotEmpty()) {
                                PlantInfoLine("Notas", plant.notas.joinToString(" · "))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherTab(
    modifier: Modifier,
    weather: List<WeatherDay>,
    isRefreshing: Boolean,
    statusMessage: String?,
    onRefresh: () -> Unit,
) {
    val today = LocalDate.now()
    val sortedWeather = remember(weather) { weather.sortedBy { it.date } }
    val pastDays = remember(sortedWeather, today) { sortedWeather.filter { it.date.isBefore(today) }.takeLast(3).reversed() }
    val futureDays = remember(sortedWeather, today) { sortedWeather.filter { it.date.isAfter(today) }.take(3) }
    val todayWeather = remember(sortedWeather, today) { sortedWeather.firstOrNull { it.date == today } }
    val latestFetch = remember(sortedWeather) { sortedWeather.maxOfOrNull { it.fetchedAt } }
    val locationName = remember(sortedWeather) { sortedWeather.firstOrNull()?.locationName ?: "Rafelbunyol" }
    val zoneId = remember { ZoneId.of("Europe/Madrid") }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item("weather_header") {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Tiempo en $locationName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                latestFetch?.let { fetchedAt ->
                    Text(
                        text = "Actualizado: ${formatTimestamp(fetchedAt, zoneId)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Actualizar tiempo")
                    }
                }
                statusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        todayWeather?.let { day ->
            item("today_weather") {
                WeatherSection(
                    title = "Hoy",
                    days = listOf(day),
                    highlightToday = true,
                )
            }
        }

        item("past_weather") {
            WeatherSection(
                title = "Lluvia últimos 3 días",
                days = pastDays,
                emptyText = "Todavía no hay histórico descargado.",
            )
        }

        item("future_weather") {
            WeatherSection(
                title = "Lluvia próximos 3 días",
                days = futureDays,
                emptyText = "Todavía no hay previsión descargada.",
            )
        }
    }
}

@Composable
private fun WeatherSection(
    title: String,
    days: List<WeatherDay>,
    emptyText: String = "Sin datos.",
    highlightToday: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (days.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            days.forEach { day ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = if (highlightToday) "Hoy, ${formatDate(day.date)}" else formatDate(day.date),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        WeatherMetricLine("Lluvia", "${formatDecimal(day.rainMm)} mm")
                        WeatherMetricLine("Precipitación total", "${formatDecimal(day.precipitationMm)} mm")
                        WeatherMetricLine(
                            "Prob. máxima",
                            day.precipitationProbabilityMax?.let { "$it%" } ?: "-",
                        )
                        WeatherMetricLine("Horas con precipitación", "${formatDecimal(day.precipitationHours)} h")
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlantInfoLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlantInfoUrlLine(
    label: String,
    url: String?,
    onOpenUrl: (String) -> Unit,
) {
    if (url.isNullOrBlank()) {
        PlantInfoLine(label, "-")
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = url,
            modifier = Modifier
                .padding(start = 12.dp)
                .clickable { onOpenUrl(url) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PlantInfoMultilineSection(
    label: String,
    lines: List<String>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        lines.forEachIndexed { index, line ->
            val indent = if (index == 0) 12.dp else 24.dp
            Text(
                text = line,
                modifier = Modifier.padding(start = indent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    statusMessage: String?,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Aún no hay recordatorios generados.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            statusMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun filterConversation(
    conversation: List<ConversationMessage>,
    showFullHistory: Boolean,
): List<ConversationMessage> {
    if (showFullHistory) return conversation
    val visibleDates = conversation.map { it.date }.distinct().takeLast(HistoryDayLimit).toSet()
    return conversation.filter { it.date in visibleDates }
}

private fun formatDate(date: LocalDate): String = date.dayOfWeek.getDisplayName(
    TextStyle.SHORT,
    Locale("es", "ES"),
).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() } +
    " ${date.dayOfMonth} " +
    date.month.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))

private fun formatDecimal(value: Double): String = if (value % 1.0 == 0.0) {
    value.toInt().toString()
} else {
    String.format(Locale.US, "%.1f", value)
}

private fun formatTimestamp(timestamp: Long, zoneId: ZoneId): String {
    val dateTime = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDateTime()
    return "${formatDate(dateTime.toLocalDate())} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

private fun android.content.Context.openUrl(url: String) {
    startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

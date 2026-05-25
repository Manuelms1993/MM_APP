package com.example.mmapp.app4.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalUriHandler
import com.example.mmapp.app4.domain.models.ScriptLogEntry
import com.example.mmapp.app4.domain.models.ScriptLogLevel
import com.example.mmapp.app4.domain.models.ScriptResultItem
import com.example.mmapp.app4.ui.HomeViewModel
import com.example.mmapp.app4.ui.ScriptCardUiState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val scripts by viewModel.scripts.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Procesos", "Logs").forEachIndexed { index, label ->
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
            0 -> ScriptsTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                scripts = scripts,
                onExecute = viewModel::executeScript,
            )

            else -> LogsTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                logs = logs,
                onClearLogs = viewModel::clearLogs,
            )
        }
    }
}

@Composable
private fun ScriptsTab(
    modifier: Modifier,
    scripts: List<ScriptCardUiState>,
    onExecute: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(scripts, key = { it.id }) { script ->
            ScriptCard(
                script = script,
                onExecute = { onExecute(script.id) },
            )
        }
    }
}

@Composable
private fun ScriptCard(
    script: ScriptCardUiState,
    onExecute: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = script.topic,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = script.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = script.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onExecute,
                enabled = !script.isRunning,
            ) {
                if (script.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                    Text("Ejecutando")
                } else {
                    Text("Ejecutar")
                }
            }
            script.lastSummary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (script.lastResults.isNotEmpty()) {
                ResultSection(items = script.lastResults)
            }
        }
    }
}

@Composable
private fun ResultSection(
    items: List<ScriptResultItem>,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Resultados",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        items.forEach { item ->
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "• ${item.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                item.linkUrl?.let { url ->
                    Text(
                        text = url,
                        modifier = Modifier.clickable { uriHandler.openUri(url) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogsTab(
    modifier: Modifier,
    logs: List<ScriptLogEntry>,
    onClearLogs: () -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Ejecución",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedButton(onClick = onClearLogs) {
                Text("Limpiar")
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Sin logs todavía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(logs) { log ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = log.message,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (log.level) {
                                ScriptLogLevel.INFO -> MaterialTheme.colorScheme.onSurface
                                ScriptLogLevel.SUCCESS -> MaterialTheme.colorScheme.primary
                                ScriptLogLevel.ERROR -> MaterialTheme.colorScheme.error
                            },
                        )
                    }
                }
            }
        }
    }
}

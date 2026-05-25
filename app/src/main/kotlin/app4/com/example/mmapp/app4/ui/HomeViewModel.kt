package com.example.mmapp.app4.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mmapp.app4.AppContainer
import com.example.mmapp.app4.domain.models.ScriptLogEntry
import com.example.mmapp.app4.domain.models.ScriptLogLevel
import com.example.mmapp.app4.domain.models.ScriptResultItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScriptCardUiState(
    val id: String,
    val topic: String,
    val title: String,
    val description: String,
    val isRunning: Boolean,
    val lastSummary: String?,
    val lastResults: List<ScriptResultItem>,
)

class HomeViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _scripts = MutableStateFlow(
        container.scripts.map { script ->
            ScriptCardUiState(
                id = script.definition.id,
                topic = script.definition.topic,
                title = script.definition.title,
                description = script.definition.description,
                isRunning = false,
                lastSummary = null,
                lastResults = emptyList(),
            )
        },
    )
    val scripts: StateFlow<List<ScriptCardUiState>> = _scripts.asStateFlow()

    private val _logs = MutableStateFlow<List<ScriptLogEntry>>(emptyList())
    val logs: StateFlow<List<ScriptLogEntry>> = _logs.asStateFlow()

    fun executeScript(scriptId: String) {
        val script = container.scripts.firstOrNull { it.definition.id == scriptId } ?: return
        updateScript(scriptId) { it.copy(isRunning = true) }
        appendLogs(
            ScriptLogEntry(
                message = "Ejecutando proceso: ${script.definition.title}",
                level = ScriptLogLevel.INFO,
            ),
        )

        viewModelScope.launch {
            val result = script.execute()
            updateScript(scriptId) {
                it.copy(
                    isRunning = false,
                    lastSummary = result.summary,
                    lastResults = result.items,
                )
            }
            appendLogs(result.logs)
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun updateScript(
        scriptId: String,
        transform: (ScriptCardUiState) -> ScriptCardUiState,
    ) {
        _scripts.value = _scripts.value.map { script ->
            if (script.id == scriptId) transform(script) else script
        }
    }

    private fun appendLogs(entries: List<ScriptLogEntry>) {
        _logs.value = _logs.value + entries
    }

    private fun appendLogs(entry: ScriptLogEntry) {
        appendLogs(listOf(entry))
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(container) as T
        }
    }
}

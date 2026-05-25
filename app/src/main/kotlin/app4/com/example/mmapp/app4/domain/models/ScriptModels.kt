package com.example.mmapp.app4.domain.models

enum class ScriptLogLevel {
    INFO,
    SUCCESS,
    ERROR,
}

data class ScriptLogEntry(
    val message: String,
    val level: ScriptLogLevel,
)

data class ScriptResultItem(
    val title: String,
    val detail: String,
    val linkUrl: String? = null,
)

data class ScriptExecutionResult(
    val summary: String,
    val logs: List<ScriptLogEntry>,
    val items: List<ScriptResultItem>,
)

data class ScriptDefinition(
    val id: String,
    val topic: String,
    val title: String,
    val description: String,
)

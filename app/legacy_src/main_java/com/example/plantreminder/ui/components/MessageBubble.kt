package com.example.plantreminder.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.plantreminder.domain.models.ConversationMessage
import com.example.plantreminder.domain.models.MessageStatus
import com.example.plantreminder.domain.models.MessageType
import com.example.plantreminder.ui.theme.LeafGreen
import com.example.plantreminder.ui.theme.RiverBlue
import com.example.plantreminder.ui.theme.SoftBlue
import com.example.plantreminder.ui.theme.SoftGreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun MessageBubble(
    message: ConversationMessage,
    maintainerFilter: String? = null,
) {
    val sections = remember(message.messageText) {
        if (message.messageType == MessageType.ACTIONS) parsePlantSections(message.messageText) else emptyList()
    }
    val expandedStates = remember(message.id) { mutableStateMapOf<String, Boolean>() }
    val responsibilityByPlant = remember(message.rawPayload) {
        parseResponsibilityByPlant(message.rawPayload)
    }
    val visibleSections = remember(sections, responsibilityByPlant, maintainerFilter) {
        filterSectionsByMaintainer(sections, responsibilityByPlant, maintainerFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (sections.isEmpty()) {
            Text(
                text = message.messageText,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
        } else {
            visibleSections.forEach { section ->
                val isExpanded = expandedStates[section.plantName] ?: false
                val style = responsibilityByPlant[section.plantName].toPlantSectionStyle()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = style.backgroundColor,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable { expandedStates[section.plantName] = !isExpanded }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = section.plantName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ResponsibilityBadge(
                                    label = style.label,
                                    backgroundColor = style.badgeBackgroundColor,
                                    contentColor = style.badgeContentColor,
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        if (isExpanded) {
                            SectionContent(section = section)
                        }
                    }
                }
            }
        }

        Text(
            text = formatStatus(message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResponsibilityBadge(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

private fun formatStatus(message: ConversationMessage): String {
    val time = Instant.ofEpochMilli(message.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    return when (message.status) {
        MessageStatus.GENERATED -> "Generado $time"
        MessageStatus.ERROR -> "Error"
    }
}

private fun parsePlantSections(text: String): List<PlantMessageSection> {
    val sections = mutableListOf<PlantMessageSection>()
    var currentPlant: String? = null
    val currentRawLines = mutableListOf<String>()

    fun flushPlant() {
        val plantName = currentPlant ?: return
        sections += PlantMessageSection(
            plantName = plantName,
            rawLines = currentRawLines.toList(),
        )
        currentPlant = null
        currentRawLines.clear()
    }

    text.lineSequence()
        .map { it.trimEnd() }
        .filter { it.isNotBlank() }
        .forEach { line ->
            val trimmedLine = line.trimStart()
            when {
                trimmedLine.startsWith("• ") -> {
                    flushPlant()
                    currentPlant = trimmedLine.removePrefix("• ").trim()
                }

                currentPlant != null -> {
                    currentRawLines += line
                }
            }
        }

    flushPlant()
    return sections
}

fun parseResponsibilityByPlant(rawPayload: String?): Map<String, String> {
    if (rawPayload.isNullOrBlank()) return emptyMap()
    return runCatching {
        Json.parseToJsonElement(rawPayload).jsonArray.mapNotNull { element ->
            val plant = element.jsonObject["planta"]?.jsonObject ?: return@mapNotNull null
            val name = plant["nombre"]?.jsonPrimitive?.content?.trim().orEmpty()
            if (name.isBlank()) return@mapNotNull null
            val responsibility = plant["responsable"]?.jsonPrimitive?.content?.trim()?.uppercase()
                ?.takeIf { it == "L" || it == "R" } ?: "L"
            name to responsibility
        }.toMap()
    }.getOrDefault(emptyMap())
}

fun messageMatchesMaintainer(
    message: ConversationMessage,
    maintainerFilter: String,
): Boolean {
    if (message.messageType != MessageType.ACTIONS) return true
    val normalizedMaintainer = maintainerFilter.uppercase()
    val responsibilityByPlant = parseResponsibilityByPlant(message.rawPayload)
    if (responsibilityByPlant.isEmpty()) return true
    return parsePlantSections(message.messageText).any { section ->
        responsibilityByPlant[section.plantName] == normalizedMaintainer
    }
}

private fun filterSectionsByMaintainer(
    sections: List<PlantMessageSection>,
    responsibilityByPlant: Map<String, String>,
    maintainerFilter: String?,
): List<PlantMessageSection> {
    val normalizedMaintainer = maintainerFilter?.uppercase() ?: return sections
    if (responsibilityByPlant.isEmpty()) return sections
    return sections.filter { responsibilityByPlant[it.plantName] == normalizedMaintainer }
}

private fun String?.toPlantSectionStyle(): PlantSectionStyle = when (this) {
    "R" -> PlantSectionStyle(
        label = "R",
        backgroundColor = SoftBlue.copy(alpha = 1f),
        badgeBackgroundColor = RiverBlue.copy(alpha = 0.22f),
        badgeContentColor = RiverBlue,
    )
    else -> PlantSectionStyle(
        label = "L",
        backgroundColor = SoftGreen.copy(alpha = 0.92f),
        badgeBackgroundColor = LeafGreen.copy(alpha = 0.18f),
        badgeContentColor = LeafGreen,
    )
}

private data class PlantSectionStyle(
    val label: String,
    val backgroundColor: Color,
    val badgeBackgroundColor: Color,
    val badgeContentColor: Color,
)

private data class PlantMessageSection(
    val plantName: String,
    val rawLines: List<String>,
)

@Composable
private fun SectionContent(
    section: PlantMessageSection,
) {
    if (section.rawLines.isEmpty()) {
        Text(
            text = "Sin detalles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        section.rawLines.forEach { line ->
            val trimmedLine = line.trimStart()
            val startPadding = when {
                trimmedLine.startsWith("· ") -> 20.dp
                else -> 0.dp
            }
            Text(
                text = trimmedLine,
                modifier = Modifier.padding(start = startPadding),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

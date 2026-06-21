package com.example.mmapp.app3.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mmapp.app3.domain.models.DaySegment
import com.example.mmapp.app3.domain.models.TravelDay
import com.example.mmapp.app3.domain.models.TravelGuide
import com.example.mmapp.app3.domain.models.TravelHotel
import com.example.mmapp.app3.domain.models.TravelLink
import com.example.mmapp.app3.domain.models.TravelRecommendation
import com.example.mmapp.app3.domain.models.TravelTopic
import com.example.mmapp.app3.ui.HomeViewModel
import com.example.mmapp.app3.ui.theme.DayCardGreen
import com.example.mmapp.app3.ui.theme.SectionBlue
import com.example.mmapp.app3.ui.theme.SectionGreen
import com.example.mmapp.app3.ui.theme.SectionLilac
import com.example.mmapp.app3.ui.theme.SectionPeach
import com.example.mmapp.app3.ui.theme.SectionSand

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val guide by viewModel.guide.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(statusMessage) {
        val message = statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Días", "Hoteles").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val currentGuide = guide
        if (currentGuide == null) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                statusMessage = statusMessage ?: "Cargando guía del viaje...",
            )
            return@Scaffold
        }

        when (selectedTab) {
            0 -> DaysTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                guide = currentGuide,
            )

            else -> HotelsTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                hotels = currentGuide.hotels,
            )
        }
    }
}

@Composable
private fun DaysTab(
    modifier: Modifier,
    guide: TravelGuide,
) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    val sectionExpandedStates = remember { mutableStateMapOf<String, Boolean>() }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(DaySectionFilter.All) }
    val filteredDays by remember(guide.days, guide.hotelsById, query, selectedFilter) {
        derivedStateOf {
            guide.days.filter { day ->
                day.matchesQuery(query) &&
                    (selectedFilter == DaySectionFilter.All || day.sectionKeys(guide.hotelsById, selectedFilter).isNotEmpty())
            }
        }
    }
    val allDaysExpanded = filteredDays.isNotEmpty() && filteredDays.all { expandedStates[it.id] == true }
    val visibleSectionKeys = filteredDays.flatMap { it.sectionKeys(guide.hotelsById, selectedFilter) }
    val allSectionsExpanded = visibleSectionKeys.isNotEmpty() && visibleSectionKeys.all { sectionExpandedStates[it] == true }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("travel_controls") {
            TravelDayControls(
                query = query,
                onQueryChange = { query = it },
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                allDaysExpanded = allDaysExpanded,
                allSectionsExpanded = allSectionsExpanded,
                onToggleAllDays = {
                    filteredDays.forEach { day ->
                        expandedStates[day.id] = !allDaysExpanded
                    }
                },
                onToggleAllSections = {
                    visibleSectionKeys.forEach { key ->
                        sectionExpandedStates[key] = !allSectionsExpanded
                    }
                },
                resultCount = filteredDays.size,
                totalCount = guide.days.size,
            )
        }

        items(filteredDays, key = { it.id }) { day ->
            val isExpanded = expandedStates[day.id] == true
            val dayIndex = guide.days.indexOfFirst { it.id == day.id }
            val previousDay = guide.days.getOrNull(dayIndex - 1)
            val nextDay = guide.days.getOrNull(dayIndex + 1)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = DayCardGreen,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedStates[day.id] = !isExpanded }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Día ${day.dayNumber} · ${day.city}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = day.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = if (isExpanded) "⌃" else "⌄",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = day.summary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    SectionSummaryRow(
                        sections = day.sectionsFor(guide.hotelsById, DaySectionFilter.All),
                    )
                    Text(
                        text = "Toca para ${if (isExpanded) "cerrar" else "abrir"} el día",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (isExpanded) {
                        DayNavigation(
                            previousDay = previousDay,
                            nextDay = nextDay,
                            onSelectDay = { selectedDay ->
                                expandedStates[day.id] = false
                                expandedStates[selectedDay.id] = true
                            },
                        )

                        day.sectionsFor(guide.hotelsById, selectedFilter).forEach { section ->
                            HorizontalDivider()
                            ExpandableSectionCard(
                                section = section,
                                expandedStates = sectionExpandedStates,
                            ) {
                                SectionContentBlock(section)
                            }
                        }
                    }
                }
            }
        }

        if (filteredDays.isEmpty()) {
            item("no_days") {
                Surface(
                    color = SectionSand,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "No hay días que coincidan con la búsqueda.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelDayControls(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: DaySectionFilter,
    onFilterChange: (DaySectionFilter) -> Unit,
    allDaysExpanded: Boolean,
    allSectionsExpanded: Boolean,
    onToggleAllDays: () -> Unit,
    onToggleAllSections: () -> Unit,
    resultCount: Int,
    totalCount: Int,
) {
    Surface(
        color = SectionSand,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Buscar") },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DaySectionFilter.entries.forEach { filter ->
                    FilterPill(
                        label = filter.label,
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                    )
                }
            }
            Text(
                text = "$resultCount/$totalCount días",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onToggleAllDays) {
                        Text(if (allDaysExpanded) "Cerrar días" else "Abrir días")
                    }
                    TextButton(onClick = onToggleAllSections) {
                        Text(if (allSectionsExpanded) "Cerrar secciones" else "Abrir secciones")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SectionSummaryRow(
    sections: List<DayUiSection>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        sections.groupBy { it.filter }.forEach { (filter, groupedSections) ->
            Surface(
                color = filter.color,
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = "${filter.label} ${groupedSections.sumOf { it.count }}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun DayNavigation(
    previousDay: TravelDay?,
    nextDay: TravelDay?,
    onSelectDay: (TravelDay) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(
            enabled = previousDay != null,
            onClick = { previousDay?.let(onSelectDay) },
        ) {
            Text(previousDay?.let { "‹ Día ${it.dayNumber}" } ?: "‹")
        }
        TextButton(
            enabled = nextDay != null,
            onClick = { nextDay?.let(onSelectDay) },
        ) {
            Text(nextDay?.let { "Día ${it.dayNumber} ›" } ?: "›")
        }
    }
}

@Composable
private fun SectionContentBlock(
    section: DayUiSection,
) {
    when (val content = section.content) {
        is DaySectionContent.Hotel -> {
            val hotel = content.hotel
            DetailBlock(
                title = "Alojamiento",
                lines = listOf(
                    hotel.name,
                    hotel.address,
                    formatHotelDays(hotel.days),
                ),
                links = hotel.sourceUrl?.let { listOf(TravelLink("Ficha hotel", it)) }.orEmpty(),
            )
        }

        is DaySectionContent.Segment -> SegmentBlock(
            segment = content.segment,
            parentKey = section.key,
        )

        is DaySectionContent.Lines -> DetailBlock(
            title = section.title,
            lines = content.lines,
        )

        is DaySectionContent.Recommendations -> RecommendationsBlock(
            recommendations = content.recommendations,
        )

        is DaySectionContent.Links -> LinksBlock(
            title = section.title,
            links = content.links,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun HotelsTab(
    modifier: Modifier,
    hotels: List<TravelHotel>,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(hotels, key = { it.id }) { hotel ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = hotel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${hotel.city} · ${formatHotelDays(hotel.days)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = hotel.address,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = hotel.status,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (hotel.notes.isNotEmpty()) {
                        hotel.notes.forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    hotel.sourceUrl?.let { url ->
                        LinkLine(link = TravelLink("Ver referencia", url))
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentBlock(
    segment: DaySegment,
    parentKey: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        segment.timeLabel?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        segment.bullets.forEach { line ->
            Text(text = "• $line", style = MaterialTheme.typography.bodyMedium)
        }
        if (segment.topics.isNotEmpty()) {
            TopicList(
                topics = segment.topics,
                parentKey = parentKey,
            )
        }
        segment.references.forEach { reference ->
            Text(
                text = "Ref: $reference",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (segment.links.isNotEmpty()) {
            LinksBlock(
                title = "Enlaces",
                links = segment.links,
            )
        }
    }
}

@Composable
private fun TopicList(
    topics: List<TravelTopic>,
    parentKey: String,
    depth: Int = 0,
) {
    val expandedStates = remember(parentKey) { mutableStateMapOf<String, Boolean>() }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        topics.forEachIndexed { index, topic ->
            TopicCard(
                topic = topic,
                topicKey = "$parentKey-$index-${topic.title}",
                expandedStates = expandedStates,
                depth = depth,
            )
        }
    }
}

@Composable
private fun TopicCard(
    topic: TravelTopic,
    topicKey: String,
    expandedStates: MutableMap<String, Boolean>,
    depth: Int,
) {
    val isExpanded = expandedStates[topicKey] == true
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (depth == 0) 0.65f else 0.45f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedStates[topicKey] = !isExpanded }
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${topic.itemCount()} ${if (isExpanded) "⌃" else "⌄"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isExpanded) {
                topic.bullets.forEach { line ->
                    Text(text = "• $line", style = MaterialTheme.typography.bodyMedium)
                }
                if (topic.topics.isNotEmpty()) {
                    TopicList(
                        topics = topic.topics,
                        parentKey = topicKey,
                        depth = depth + 1,
                    )
                }
                topic.links.forEach { link ->
                    LinkLine(link = link)
                }
            }
        }
    }
}

@Composable
private fun DetailBlock(
    title: String,
    lines: List<String>,
    links: List<TravelLink> = emptyList(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        lines.forEach { line ->
            Text(text = "• $line", style = MaterialTheme.typography.bodyMedium)
        }
        links.forEach { link ->
            LinkLine(link = link)
        }
    }
}

@Composable
private fun ExpandableSectionCard(
    section: DayUiSection,
    expandedStates: MutableMap<String, Boolean>,
    content: @Composable () -> Unit,
) {
    val isExpanded = expandedStates[section.key] == true
    Surface(
        color = section.filter.color,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedStates[section.key] = !isExpanded }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${section.filter.marker} ${section.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${section.count} ${if (isExpanded) "⌃" else "⌄"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isExpanded) {
                content()
            }
        }
    }
}

@Composable
private fun LinksBlock(
    title: String,
    links: List<TravelLink>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        links.forEach { link ->
            LinkLine(link = link)
        }
    }
}

@Composable
private fun RecommendationsBlock(
    recommendations: List<TravelRecommendation>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Recomendaciones",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        recommendations.forEach { recommendation ->
            Text(
                text = "• ${recommendation.text}",
                style = MaterialTheme.typography.bodyMedium,
            )
            recommendation.links.forEach { link ->
                LinkLine(link = link)
            }
        }
    }
}

@Composable
private fun LinkLine(
    link: TravelLink,
) {
    val context = LocalContext.current
    Text(
        text = "${link.label}: ${link.url}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        },
    )
}

@Composable
private fun EmptyState(
    modifier: Modifier,
    statusMessage: String,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Guía de viaje",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
    }
}

private enum class DaySectionFilter(
    val label: String,
    val marker: String,
    val color: Color,
) {
    All("Todo", "•", SectionLilac),
    Warnings("Advertencias", "!", Color(0xFFFFF4D6)),
    Food("Comida", "+", SectionPeach),
    Route("Ruta", ">", SectionGreen),
    Hotel("Hotel", "^", SectionSand),
    Links("Enlaces", "@", SectionBlue),
    Pending("Pendientes", "?", Color(0xFFF0F0F0)),
}

private data class DayUiSection(
    val key: String,
    val title: String,
    val filter: DaySectionFilter,
    val count: Int,
    val content: DaySectionContent,
)

private sealed interface DaySectionContent {
    data class Hotel(val hotel: TravelHotel) : DaySectionContent
    data class Segment(val segment: DaySegment) : DaySectionContent
    data class Lines(val lines: List<String>) : DaySectionContent
    data class Recommendations(val recommendations: List<TravelRecommendation>) : DaySectionContent
    data class Links(val links: List<TravelLink>) : DaySectionContent
}

private fun TravelDay.sectionsFor(
    hotelsById: Map<String, TravelHotel>,
    filter: DaySectionFilter,
): List<DayUiSection> {
    val sections = buildList {
        overnightHotelId?.let(hotelsById::get)?.let { hotel ->
            add(
                DayUiSection(
                    key = "$id-hotel",
                    title = "Alojamiento",
                    filter = DaySectionFilter.Hotel,
                    count = 1,
                    content = DaySectionContent.Hotel(hotel),
                ),
            )
        }

        segments.forEachIndexed { index, segment ->
            val sectionFilter = segment.sectionFilter()
            add(
                DayUiSection(
                    key = "$id-segment-$index",
                    title = segment.title,
                    filter = sectionFilter,
                    count = segment.itemCount().coerceAtLeast(1),
                    content = DaySectionContent.Segment(segment),
                ),
            )
        }

        if (foodSuggestions.isNotEmpty()) {
            add(
                DayUiSection(
                    key = "$id-food",
                    title = "Comida",
                    filter = DaySectionFilter.Food,
                    count = foodSuggestions.size,
                    content = DaySectionContent.Lines(foodSuggestions),
                ),
            )
        }

        if (notes.isNotEmpty()) {
            add(
                DayUiSection(
                    key = "$id-notes",
                    title = "Notas",
                    filter = if (notes.any { it.contains("pendiente", ignoreCase = true) }) DaySectionFilter.Pending else DaySectionFilter.Route,
                    count = notes.size,
                    content = DaySectionContent.Lines(notes),
                ),
            )
        }

        if (recommendations.isNotEmpty()) {
            add(
                DayUiSection(
                    key = "$id-recommendations",
                    title = "Recomendaciones",
                    filter = DaySectionFilter.Route,
                    count = recommendations.size,
                    content = DaySectionContent.Recommendations(recommendations),
                ),
            )
        }

        if (links.isNotEmpty()) {
            add(
                DayUiSection(
                    key = "$id-links",
                    title = "Enlaces generales",
                    filter = DaySectionFilter.Links,
                    count = links.size,
                    content = DaySectionContent.Links(links),
                ),
            )
        }
    }
    return if (filter == DaySectionFilter.All) sections else sections.filter { it.filter == filter }
}

private fun TravelDay.sectionKeys(
    hotelsById: Map<String, TravelHotel>,
    filter: DaySectionFilter,
): List<String> = sectionsFor(hotelsById, filter).map { it.key }

private fun DaySegment.sectionFilter(): DaySectionFilter = when {
    title.equals("Advertencias", ignoreCase = true) -> DaySectionFilter.Warnings
    title.equals("Comida", ignoreCase = true) -> DaySectionFilter.Food
    containsPendingText() -> DaySectionFilter.Pending
    links.isNotEmpty() && bullets.isEmpty() && topics.isEmpty() -> DaySectionFilter.Links
    else -> DaySectionFilter.Route
}

private fun DaySegment.containsPendingText(): Boolean =
    title.contains("pendiente", ignoreCase = true) ||
        bullets.any { it.contains("pendiente", ignoreCase = true) } ||
        references.any { it.contains("pendiente", ignoreCase = true) } ||
        topics.any { it.containsPendingText() }

private fun TravelTopic.containsPendingText(): Boolean =
    title.contains("pendiente", ignoreCase = true) ||
        bullets.any { it.contains("pendiente", ignoreCase = true) } ||
        topics.any { it.containsPendingText() }

private fun DaySegment.itemCount(): Int =
    bullets.size + topics.sumOf { it.itemCount() } + references.size + links.size

private fun TravelTopic.itemCount(): Int =
    bullets.size + topics.sumOf { it.itemCount() } + links.size

private fun TravelDay.matchesQuery(query: String): Boolean {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isBlank()) return true
    return listOf(id, title, city, summary).any { it.lowercase().contains(normalizedQuery) } ||
        segments.any { it.matchesQuery(normalizedQuery) } ||
        foodSuggestions.any { it.lowercase().contains(normalizedQuery) } ||
        notes.any { it.lowercase().contains(normalizedQuery) } ||
        recommendations.any { recommendation ->
            recommendation.text.lowercase().contains(normalizedQuery) ||
                recommendation.links.any { it.matchesQuery(normalizedQuery) }
        } ||
        links.any { it.matchesQuery(normalizedQuery) }
}

private fun DaySegment.matchesQuery(query: String): Boolean =
    title.lowercase().contains(query) ||
        timeLabel.orEmpty().lowercase().contains(query) ||
        bullets.any { it.lowercase().contains(query) } ||
        references.any { it.lowercase().contains(query) } ||
        topics.any { it.matchesQuery(query) } ||
        links.any { it.matchesQuery(query) }

private fun TravelTopic.matchesQuery(query: String): Boolean =
    title.lowercase().contains(query) ||
        bullets.any { it.lowercase().contains(query) } ||
        topics.any { it.matchesQuery(query) } ||
        links.any { it.matchesQuery(query) }

private fun TravelLink.matchesQuery(query: String): Boolean =
    label.lowercase().contains(query) || url.lowercase().contains(query)

private fun formatHotelDays(days: List<Int>): String {
    if (days.isEmpty()) return "Días no definidos"
    if (days.size == 1) return "Día ${days.first()}"
    val sortedDays = days.sorted()
    return "Días ${sortedDays.first()}-${sortedDays.last()}"
}

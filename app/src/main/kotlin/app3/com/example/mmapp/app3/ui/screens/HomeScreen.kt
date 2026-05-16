package com.example.mmapp.app3.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.example.mmapp.app3.ui.HomeViewModel
import com.example.mmapp.app3.ui.theme.DayCardGreen
import com.example.mmapp.app3.ui.theme.SectionBlue
import com.example.mmapp.app3.ui.theme.SectionGreen
import com.example.mmapp.app3.ui.theme.SectionLilac
import com.example.mmapp.app3.ui.theme.SectionPeach
import com.example.mmapp.app3.ui.theme.SectionSand
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(guide.days, key = { it.id }) { day ->
            val isExpanded = expandedStates[day.id] == true
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
                    Text(
                        text = "Día ${day.dayNumber} · ${day.city}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${day.date.format(DayFormatter)} · ${day.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = day.summary,
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    if (isExpanded) {
                        day.overnightHotelId?.let { hotelId ->
                            guide.hotelsById[hotelId]?.let { hotel ->
                                HorizontalDivider()
                                ExpandableSectionCard(
                                    title = "Alojamiento",
                                    containerColor = SectionSand,
                                    sectionKey = "${day.id}-hotel",
                                    expandedStates = sectionExpandedStates,
                                ) {
                                    DetailBlock(
                                        title = "Alojamiento",
                                        lines = listOf(
                                            hotel.name,
                                            hotel.address,
                                            "${hotel.checkIn} -> ${hotel.checkOut} · ${hotel.priceLabel}",
                                        ),
                                        links = hotel.sourceUrl?.let { listOf(TravelLink("Ficha hotel", it)) }.orEmpty(),
                                    )
                                }
                            }
                        }

                        day.segments.forEachIndexed { index, segment ->
                            HorizontalDivider()
                            ExpandableSectionCard(
                                title = segment.title,
                                containerColor = SectionGreen,
                                sectionKey = "${day.id}-segment-$index",
                                expandedStates = sectionExpandedStates,
                            ) {
                                SegmentBlock(segment = segment)
                            }
                        }

                        if (day.foodSuggestions.isNotEmpty()) {
                            HorizontalDivider()
                            ExpandableSectionCard(
                                title = "Comida",
                                containerColor = SectionPeach,
                                sectionKey = "${day.id}-food",
                                expandedStates = sectionExpandedStates,
                            ) {
                                DetailBlock(
                                    title = "Comida",
                                    lines = day.foodSuggestions,
                                )
                            }
                        }

                        if (day.notes.isNotEmpty()) {
                            HorizontalDivider()
                            ExpandableSectionCard(
                                title = "Notas",
                                containerColor = SectionBlue,
                                sectionKey = "${day.id}-notes",
                                expandedStates = sectionExpandedStates,
                            ) {
                                DetailBlock(
                                    title = "Notas",
                                    lines = day.notes,
                                )
                            }
                        }

                        if (day.recommendations.isNotEmpty()) {
                            HorizontalDivider()
                            ExpandableSectionCard(
                                title = "Recomendaciones",
                                containerColor = SectionLilac,
                                sectionKey = "${day.id}-recommendations",
                                expandedStates = sectionExpandedStates,
                            ) {
                                RecommendationsBlock(
                                    recommendations = day.recommendations,
                                )
                            }
                        }

                        if (day.links.isNotEmpty()) {
                            HorizontalDivider()
                            ExpandableSectionCard(
                                title = "Enlaces generales",
                                containerColor = SectionLilac,
                                sectionKey = "${day.id}-links",
                                expandedStates = sectionExpandedStates,
                            ) {
                                LinksBlock(
                                    title = "Enlaces generales",
                                    links = day.links,
                                )
                            }
                        }
                    }
                }
            }
        }
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
                        text = "${hotel.city} · ${hotel.checkIn} -> ${hotel.checkOut}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = hotel.address,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${hotel.status} · ${hotel.priceLabel}",
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
    title: String,
    containerColor: Color,
    sectionKey: String,
    expandedStates: MutableMap<String, Boolean>,
    content: @Composable () -> Unit,
) {
    val isExpanded = expandedStates[sectionKey] == true
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedStates[sectionKey] = !isExpanded }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (isExpanded) "Ocultar" else "Ver",
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

private val DayFormatter = DateTimeFormatter.ofPattern("EEEE d MMM", Locale("es", "ES"))

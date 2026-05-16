package com.example.mmapp.app2.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mmapp.R
import com.example.mmapp.app2.domain.models.ConversationMessage
import com.example.mmapp.app2.domain.models.ShoppingList
import com.example.mmapp.app2.domain.models.ShoppingListItem
import com.example.mmapp.app2.ui.HomeViewModel
import com.example.mmapp.app2.ui.components.DaySection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val HistoryDayLimit = 30

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val showFullHistory by viewModel.showFullHistory.collectAsStateWithLifecycle()
    val warningMessage by viewModel.warningMessage.collectAsStateWithLifecycle()
    val shoppingList by viewModel.shoppingList.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val visibleConversation = remember(conversation, showFullHistory) {
        filterConversation(conversation, showFullHistory)
    }
    val groupedConversation = remember(visibleConversation) {
        visibleConversation.groupBy { it.date }.toSortedMap(compareByDescending { it })
    }
    val hasHiddenHistory = remember(conversation, groupedConversation) {
        conversation.groupBy { it.date }.size > groupedConversation.size
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

    LaunchedEffect(selectedTab, lifecycleState) {
        if (selectedTab != 0 || !lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) return@LaunchedEffect
        while (true) {
            viewModel.ensureMenuFresh()
            delay(2_000)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Menú", "Compra").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
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
                    }
                }
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            0 -> MenuTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                groupedConversation = groupedConversation,
                listState = listState,
                hasHiddenHistory = hasHiddenHistory,
                onShowFullHistory = viewModel::showFullHistory,
                statusMessage = statusMessage,
            )

            else -> ShoppingTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                shoppingList = shoppingList,
                isBusy = isBusy,
                onCalculate = viewModel::calculateShoppingList,
            )
        }
    }
}

@Composable
private fun MenuTab(
    modifier: Modifier,
    groupedConversation: Map<LocalDate, List<ConversationMessage>>,
    listState: LazyListState,
    hasHiddenHistory: Boolean,
    onShowFullHistory: () -> Unit,
    statusMessage: String?,
) {
    if (groupedConversation.isEmpty()) {
        EmptyState(modifier = modifier, statusMessage = statusMessage)
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
                    DaySection(date = date, messages = messages)
                }
            }

            if (hasHiddenHistory) {
                item("show_history") {
                    TextButton(
                        onClick = onShowFullHistory,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Mostrar más días")
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
private fun ShoppingTab(
    modifier: Modifier,
    shoppingList: ShoppingList?,
    isBusy: Boolean,
    onCalculate: () -> Unit,
) {
    if (shoppingList == null) {
        LogoEmptyBackground(modifier = modifier) {
            Button(
                onClick = onCalculate,
                enabled = !isBusy,
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Calcular lista de la compra")
                }
            }
        }
        return
    }

    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM") }
    val locale = remember { Locale("es", "ES") }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("button") {
            Button(
                onClick = onCalculate,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Calcular lista de la compra")
                }
            }
        }

        item("period") {
            val fromDay = shoppingList.fromDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            val toDay = shoppingList.toDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            Text(
                text = "Periodo: ${fromDay.replaceFirstChar { it.titlecase(locale) }} ${shoppingList.fromDate.format(formatter)} - ${toDay.replaceFirstChar { it.titlecase(locale) }} ${shoppingList.toDate.format(formatter)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (shoppingList.items.isEmpty()) {
            item("empty") {
                Text(
                    text = "No hay ingredientes calculados para este periodo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                count = shoppingList.items.size,
                key = { index -> shoppingList.items[index].ingredient },
            ) { index ->
                ShoppingListExpandableItem(
                    item = shoppingList.items[index],
                    locale = locale,
                    formatter = formatter,
                )
            }
        }
    }
}

@Composable
private fun ShoppingListExpandableItem(
    item: ShoppingListItem,
    locale: Locale,
    formatter: DateTimeFormatter,
) {
    var expanded by rememberSaveable(item.ingredient) { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${item.ingredient} x${item.occurrences}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = if (expanded) "Ocultar" else "Ver origen",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (expanded) {
                item.sources.forEach { source ->
                    val dayName = source.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                    Text(
                        text = "• ${dayName.replaceFirstChar { it.titlecase(locale) }} ${source.date.format(formatter)} · ${source.mealLabel}: ${source.optionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    statusMessage: String?,
) {
    LogoEmptyBackground(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Aún no hay menús generados.",
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

@Composable
private fun LogoEmptyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.62f)
                .alpha(0.08f),
            contentScale = ContentScale.Fit,
        )
        content()
    }
}

private fun filterConversation(
    conversation: List<ConversationMessage>,
    showFullHistory: Boolean,
): List<ConversationMessage> {
    if (showFullHistory) return conversation
    val visibleDates = conversation.map { it.date }.distinct().take(HistoryDayLimit).toSet()
    return conversation.filter { it.date in visibleDates }
}

package com.example.mmapp.app2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mmapp.app2.AppContainer
import com.example.mmapp.app2.domain.MessageBuilder
import com.example.mmapp.app2.domain.models.ConversationMessage
import com.example.mmapp.app2.domain.models.ShoppingList
import com.example.mmapp.app2.domain.usecases.CalculateShoppingListUseCase
import com.example.mmapp.app2.ui.usecases.GeneratePendingMenuUseCase
import com.example.mmapp.app2.ui.usecases.LoadMenuCatalogUseCase
import com.example.mmapp.app2.ui.usecases.ObserveConversationUseCase
import com.example.mmapp.app2.ui.usecases.ObserveMenuLoadWarningsUseCase
import com.example.mmapp.app2.ui.usecases.SyncMenuInputsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

class HomeViewModel(
    observeConversationUseCase: ObserveConversationUseCase,
    private val loadMenuCatalogUseCase: LoadMenuCatalogUseCase,
    observeMenuLoadWarningsUseCase: ObserveMenuLoadWarningsUseCase,
    private val generatePendingMenuUseCase: GeneratePendingMenuUseCase,
    private val syncMenuInputsUseCase: SyncMenuInputsUseCase,
    private val calculateShoppingListUseCase: CalculateShoppingListUseCase,
) : ViewModel() {
    private val autoRefreshMutex = Mutex()
    private val messageBuilder = MessageBuilder()

    val conversation: StateFlow<List<ConversationMessage>> = observeConversationUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _showFullHistory = MutableStateFlow(false)
    val showFullHistory: StateFlow<Boolean> = _showFullHistory.asStateFlow()

    private val _warningMessage = MutableStateFlow<String?>(null)
    val warningMessage: StateFlow<String?> = _warningMessage.asStateFlow()

    private val _shoppingList = MutableStateFlow<ShoppingList?>(null)
    val shoppingList: StateFlow<ShoppingList?> = _shoppingList.asStateFlow()

    init {
        viewModelScope.launch {
            observeMenuLoadWarningsUseCase().collect { _warningMessage.value = it }
        }
        viewModelScope.launch { warmUpCatalog() }
    }

    fun generatePending() {
        launchBusyAction {
            val status = generatePendingMenuUseCase()
            _showFullHistory.value = false
            _statusMessage.value = status
        }
    }

    fun syncRemoteInputs() {
        launchBusyAction {
            val status = syncMenuInputsUseCase()
            _statusMessage.value = status
        }
    }

    fun calculateShoppingList() {
        launchBusyAction {
            _shoppingList.value = calculateShoppingListUseCase()
            _statusMessage.value = null
        }
    }

    fun showFullHistory() {
        _showFullHistory.value = true
    }

    fun dismissWarning() {
        _warningMessage.value = null
    }

    suspend fun ensureMenuFresh() {
        runCatching {
            autoRefreshMutex.withLock {
                val today = LocalDate.now()
                val endDate = today.plusDays(7)
                val catalog = loadMenuCatalogUseCase()
                val currentMessagesByDate = conversation.value.associateBy { it.date }
                val expectedDates = generateSequence(today) { current ->
                    current.plusDays(1).takeUnless { it.isAfter(endDate) }
                }.toList()

                val isUpToDate = expectedDates.size == currentMessagesByDate.size &&
                    expectedDates.all { date ->
                        val expectedText = messageBuilder.build(catalog.selectionForDate(date)).text
                        currentMessagesByDate[date]?.messageText == expectedText
                    }

                if (!isUpToDate) {
                    generatePendingMenuUseCase(
                        startDate = today,
                        endDate = endDate,
                        silent = true,
                    )
                }
            }
        }.onFailure { throwable ->
            _statusMessage.value = throwable.message ?: "No se pudo refrescar el menú."
        }
    }

    private suspend fun warmUpCatalog() {
        runCatching { loadMenuCatalogUseCase() }
            .onFailure { _statusMessage.value = it.message ?: "No se pudo cargar el menú." }
    }

    private fun launchBusyAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                block()
            } catch (t: Throwable) {
                _statusMessage.value = t.message ?: "Ha fallado la operación."
            }
            _isBusy.value = false
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
                observeConversationUseCase = container.observeConversationUseCase,
                loadMenuCatalogUseCase = container.loadMenuCatalogUseCase,
                observeMenuLoadWarningsUseCase = container.observeMenuLoadWarningsUseCase,
                generatePendingMenuUseCase = container.generatePendingMenuUseCase,
                syncMenuInputsUseCase = container.syncMenuInputsUseCase,
                calculateShoppingListUseCase = container.calculateShoppingListUseCase,
            ) as T
        }
    }
}

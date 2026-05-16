package com.example.mmapp.app1.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mmapp.app1.AppContainer
import com.example.mmapp.app1.domain.models.ConversationMessage
import com.example.mmapp.app1.domain.models.PlantDefinition
import com.example.mmapp.app1.domain.models.WeatherDay
import com.example.mmapp.app1.ui.usecases.GeneratePendingCareUseCase
import com.example.mmapp.app1.ui.usecases.LoadPlantsUseCase
import com.example.mmapp.app1.ui.usecases.ObserveActiveMaintainerUseCase
import com.example.mmapp.app1.ui.usecases.ObserveConversationUseCase
import com.example.mmapp.app1.ui.usecases.ObservePlantLoadWarningsUseCase
import com.example.mmapp.app1.ui.usecases.ObserveWeatherUseCase
import com.example.mmapp.app1.ui.usecases.RefreshWeatherUseCase
import com.example.mmapp.app1.ui.usecases.SetActiveMaintainerUseCase
import com.example.mmapp.app1.ui.usecases.SyncPlantInputsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    observeConversationUseCase: ObserveConversationUseCase,
    private val loadPlantsUseCase: LoadPlantsUseCase,
    observePlantLoadWarningsUseCase: ObservePlantLoadWarningsUseCase,
    observeWeatherUseCase: ObserveWeatherUseCase,
    observeActiveMaintainerUseCase: ObserveActiveMaintainerUseCase,
    private val generatePendingCareUseCase: GeneratePendingCareUseCase,
    private val syncPlantInputsUseCase: SyncPlantInputsUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val setActiveMaintainerUseCase: SetActiveMaintainerUseCase,
) : ViewModel() {
    val conversation: StateFlow<List<ConversationMessage>> = observeConversationUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weather: StateFlow<List<WeatherDay>> = observeWeatherUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeMaintainer: StateFlow<String> = observeActiveMaintainerUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "L")

    private val _plants = MutableStateFlow<List<PlantDefinition>>(emptyList())
    val plants: StateFlow<List<PlantDefinition>> = _plants.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _showFullHistory = MutableStateFlow(false)
    val showFullHistory: StateFlow<Boolean> = _showFullHistory.asStateFlow()

    private val _warningMessage = MutableStateFlow<String?>(null)
    val warningMessage: StateFlow<String?> = _warningMessage.asStateFlow()

    private val _isWeatherRefreshing = MutableStateFlow(false)
    val isWeatherRefreshing: StateFlow<Boolean> = _isWeatherRefreshing.asStateFlow()

    private val _weatherStatusMessage = MutableStateFlow<String?>(null)
    val weatherStatusMessage: StateFlow<String?> = _weatherStatusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            observePlantLoadWarningsUseCase().collect { _warningMessage.value = it }
        }
        viewModelScope.launch { reloadPlants() }
        refreshWeather(silent = true)
    }

    fun generatePending() {
        launchBusyAction {
            val status = generatePendingCareUseCase()
            reloadPlants()
            _showFullHistory.value = false
            _statusMessage.value = status
        }
    }

    fun syncRemoteInputs() {
        launchBusyAction {
            val status = syncPlantInputsUseCase()
            reloadPlants()
            _statusMessage.value = status
        }
    }

    fun showFullHistory() {
        _showFullHistory.value = true
    }

    fun dismissWarning() {
        _warningMessage.value = null
    }

    fun refreshWeather() {
        refreshWeather(silent = false)
    }

    fun setActiveMaintainer(maintainer: String) {
        viewModelScope.launch {
            runCatching { setActiveMaintainerUseCase(maintainer) }
                .onFailure { _statusMessage.value = it.message ?: "No se pudo guardar el responsable activo." }
        }
    }

    private suspend fun reloadPlants() {
        runCatching { loadPlantsUseCase() }
            .onSuccess { _plants.value = it }
            .onFailure { _statusMessage.value = it.message ?: "No se pudieron cargar las plantas." }
    }

    private fun refreshWeather(silent: Boolean) {
        viewModelScope.launch {
            _isWeatherRefreshing.value = true
            if (!silent) _weatherStatusMessage.value = null
            runCatching { refreshWeatherUseCase() }
                .onSuccess { result ->
                    if (!silent) {
                        _weatherStatusMessage.value = "Tiempo actualizado para ${result.locationName}."
                    }
                }
                .onFailure { throwable ->
                    _weatherStatusMessage.value = throwable.message ?: "No se pudo actualizar el tiempo."
                }
            _isWeatherRefreshing.value = false
        }
    }

    private fun launchBusyAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            _statusMessage.value = null
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
                loadPlantsUseCase = container.loadPlantsUseCase,
                observePlantLoadWarningsUseCase = container.observePlantLoadWarningsUseCase,
                observeWeatherUseCase = container.observeWeatherUseCase,
                observeActiveMaintainerUseCase = container.observeActiveMaintainerUseCase,
                generatePendingCareUseCase = container.generatePendingCareUseCase,
                syncPlantInputsUseCase = container.syncPlantInputsUseCase,
                refreshWeatherUseCase = container.refreshWeatherUseCase,
                setActiveMaintainerUseCase = container.setActiveMaintainerUseCase,
            ) as T
        }
    }
}

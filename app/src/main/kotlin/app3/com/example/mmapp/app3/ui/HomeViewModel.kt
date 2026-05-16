package com.example.mmapp.app3.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mmapp.app3.AppContainer
import com.example.mmapp.app3.domain.models.TravelGuide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _guide = MutableStateFlow<TravelGuide?>(null)
    val guide: StateFlow<TravelGuide?> = _guide.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { container.travelGuideRepository.getGuide() }
                .onSuccess { _guide.value = it }
                .onFailure { _statusMessage.value = it.message ?: "No se pudo cargar la guía del viaje." }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(container) as T
        }
    }
}

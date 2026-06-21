package com.example.mmapp.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mmapp.settings.AppContainer
import com.example.mmapp.settings.data.repositories.AppNotificationSettings
import com.example.mmapp.settings.data.repositories.ProcessSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val appId: String,
    val title: String,
    val enabled: Boolean,
    val intervalDays: Int,
    val hourOfDay: Int,
)

data class ProcessSettingsUiState(
    val processId: String,
    val title: String,
    val enabled: Boolean,
    val intervalDays: Int,
    val hourOfDay: Int,
)

class SettingsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    val appNotificationSettings: StateFlow<List<NotificationSettingsUiState>> =
        container.appSettingsRepository.observeNotificationSettings()
            .map { settings -> settings.appNotifications.map { it.toUiState() } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

    val processSettings: StateFlow<List<ProcessSettingsUiState>> =
        container.appSettingsRepository.observeProcessSettings()
            .map { list -> list.map { it.toUiState() } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList(),
            )

    fun setPlantNotificationsEnabled(enabled: Boolean, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setPlantNotificationsEnabled(enabled)
            onSaved()
        }
    }

    fun setPlantNotificationIntervalDays(intervalDays: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setPlantNotificationIntervalDays(intervalDays)
            onSaved()
        }
    }

    fun setPlantNotificationHourOfDay(hourOfDay: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setPlantNotificationHourOfDay(hourOfDay)
            onSaved()
        }
    }

    fun setFoodNotificationsEnabled(enabled: Boolean, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setFoodNotificationsEnabled(enabled)
            onSaved()
        }
    }

    fun setFoodNotificationIntervalDays(intervalDays: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setFoodNotificationIntervalDays(intervalDays)
            onSaved()
        }
    }

    fun setFoodNotificationHourOfDay(hourOfDay: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setFoodNotificationHourOfDay(hourOfDay)
            onSaved()
        }
    }

    fun setLunchNotificationsEnabled(enabled: Boolean, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setLunchNotificationsEnabled(enabled)
            onSaved()
        }
    }

    fun setLunchNotificationIntervalDays(intervalDays: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setLunchNotificationIntervalDays(intervalDays)
            onSaved()
        }
    }

    fun setLunchNotificationHourOfDay(hourOfDay: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setLunchNotificationHourOfDay(hourOfDay)
            onSaved()
        }
    }

    fun setDinnerNotificationsEnabled(enabled: Boolean, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setDinnerNotificationsEnabled(enabled)
            onSaved()
        }
    }

    fun setDinnerNotificationIntervalDays(intervalDays: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setDinnerNotificationIntervalDays(intervalDays)
            onSaved()
        }
    }

    fun setDinnerNotificationHourOfDay(hourOfDay: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setDinnerNotificationHourOfDay(hourOfDay)
            onSaved()
        }
    }

    fun saveNotificationSettings(settings: List<NotificationSettingsUiState>, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.saveNotificationSettings(settings.map { it.toDomain() })
            onSaved()
        }
    }

    fun setProcessEnabled(processId: String, enabled: Boolean, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setProcessEnabled(processId, enabled)
            onSaved()
        }
    }

    fun setProcessIntervalDays(processId: String, intervalDays: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setProcessIntervalDays(processId, intervalDays)
            onSaved()
        }
    }

    fun setProcessHourOfDay(processId: String, hourOfDay: Int, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.setProcessHourOfDay(processId, hourOfDay)
            onSaved()
        }
    }

    fun saveProcessSettings(settings: List<ProcessSettingsUiState>, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            container.appSettingsRepository.saveProcessSettings(settings.map { it.toDomain() })
            onSaved()
        }
    }

    private fun AppNotificationSettings.toUiState(): NotificationSettingsUiState = NotificationSettingsUiState(
        appId = appId,
        title = title,
        enabled = enabled,
        intervalDays = intervalDays,
        hourOfDay = hourOfDay,
    )

    private fun ProcessSettings.toUiState(): ProcessSettingsUiState = ProcessSettingsUiState(
        processId = processId,
        title = title,
        enabled = enabled,
        intervalDays = intervalDays,
        hourOfDay = hourOfDay,
    )

    private fun NotificationSettingsUiState.toDomain(): AppNotificationSettings = AppNotificationSettings(
        appId = appId,
        title = title,
        enabled = enabled,
        intervalDays = intervalDays,
        hourOfDay = hourOfDay,
    )

    private fun ProcessSettingsUiState.toDomain(): ProcessSettings = ProcessSettings(
        processId = processId,
        title = title,
        enabled = enabled,
        intervalDays = intervalDays,
        hourOfDay = hourOfDay,
    )

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(container) as T
        }
    }
}

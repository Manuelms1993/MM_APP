package com.example.plantreminder

import com.example.plantreminder.data.repositories.ConversationDataSource
import com.example.plantreminder.data.repositories.PlantCatalogDataSource
import com.example.plantreminder.data.repositories.WeatherDataSource
import com.example.plantreminder.data.repositories.AppPreferencesDataSource
import com.example.plantreminder.domain.usecases.GetMaintainerPendingActionsUseCase
import com.example.plantreminder.ui.usecases.GeneratePendingCareUseCase
import com.example.plantreminder.ui.usecases.LoadPlantsUseCase
import com.example.plantreminder.ui.usecases.ObserveActiveMaintainerUseCase
import com.example.plantreminder.ui.usecases.ObserveConversationUseCase
import com.example.plantreminder.ui.usecases.ObservePlantLoadWarningsUseCase
import com.example.plantreminder.ui.usecases.ObserveWeatherUseCase
import com.example.plantreminder.ui.usecases.RefreshWeatherUseCase
import com.example.plantreminder.ui.usecases.SetActiveMaintainerUseCase
import com.example.plantreminder.ui.usecases.SyncPlantInputsUseCase

data class AppContainer(
    val observeConversationUseCase: ObserveConversationUseCase,
    val loadPlantsUseCase: LoadPlantsUseCase,
    val observePlantLoadWarningsUseCase: ObservePlantLoadWarningsUseCase,
    val generatePendingCareUseCase: GeneratePendingCareUseCase,
    val syncPlantInputsUseCase: SyncPlantInputsUseCase,
    val observeWeatherUseCase: ObserveWeatherUseCase,
    val refreshWeatherUseCase: RefreshWeatherUseCase,
    val observeActiveMaintainerUseCase: ObserveActiveMaintainerUseCase,
    val setActiveMaintainerUseCase: SetActiveMaintainerUseCase,
    val plantCatalogDataSource: PlantCatalogDataSource,
    val conversationDataSource: ConversationDataSource,
    val weatherDataSource: WeatherDataSource,
    val appPreferencesDataSource: AppPreferencesDataSource,
    val getMaintainerPendingActionsUseCase: GetMaintainerPendingActionsUseCase,
)

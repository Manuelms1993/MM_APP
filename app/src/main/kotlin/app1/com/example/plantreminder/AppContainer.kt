package com.example.mmapp.app1

import com.example.mmapp.app1.data.repositories.ConversationDataSource
import com.example.mmapp.app1.data.repositories.PlantCatalogDataSource
import com.example.mmapp.app1.data.repositories.WeatherDataSource
import com.example.mmapp.app1.data.repositories.AppPreferencesDataSource
import com.example.mmapp.app1.domain.usecases.GetMaintainerPendingActionsUseCase
import com.example.mmapp.app1.ui.usecases.GeneratePendingCareUseCase
import com.example.mmapp.app1.ui.usecases.LoadPlantsUseCase
import com.example.mmapp.app1.ui.usecases.ObserveActiveMaintainerUseCase
import com.example.mmapp.app1.ui.usecases.ObserveConversationUseCase
import com.example.mmapp.app1.ui.usecases.ObservePlantLoadWarningsUseCase
import com.example.mmapp.app1.ui.usecases.ObserveWeatherUseCase
import com.example.mmapp.app1.ui.usecases.RefreshWeatherUseCase
import com.example.mmapp.app1.ui.usecases.SetActiveMaintainerUseCase
import com.example.mmapp.app1.ui.usecases.SyncPlantInputsUseCase

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

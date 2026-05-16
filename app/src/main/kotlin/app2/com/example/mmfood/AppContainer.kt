package com.example.mmapp.app2

import com.example.mmapp.app2.data.repositories.ConversationDataSource
import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.usecases.CalculateShoppingListUseCase
import com.example.mmapp.app2.ui.usecases.GeneratePendingMenuUseCase
import com.example.mmapp.app2.ui.usecases.LoadMenuCatalogUseCase
import com.example.mmapp.app2.ui.usecases.ObserveConversationUseCase
import com.example.mmapp.app2.ui.usecases.ObserveMenuLoadWarningsUseCase
import com.example.mmapp.app2.ui.usecases.SyncMenuInputsUseCase

data class AppContainer(
    val observeConversationUseCase: ObserveConversationUseCase,
    val loadMenuCatalogUseCase: LoadMenuCatalogUseCase,
    val observeMenuLoadWarningsUseCase: ObserveMenuLoadWarningsUseCase,
    val generatePendingMenuUseCase: GeneratePendingMenuUseCase,
    val syncMenuInputsUseCase: SyncMenuInputsUseCase,
    val calculateShoppingListUseCase: CalculateShoppingListUseCase,
    val menuCatalogDataSource: MenuCatalogDataSource,
    val conversationDataSource: ConversationDataSource,
)

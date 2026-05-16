package com.example.mmapp.app1.ui.usecases

import com.example.mmapp.app1.data.repositories.PlantCatalogDataSource
import com.example.mmapp.app1.domain.models.MessageSource
import com.example.mmapp.app1.domain.usecases.GeneratePendingMessagesUseCase
import com.example.mmapp.app1.ui.HomeOperationMessageFormatter

class GeneratePendingCareUseCase(
    private val plantCatalogDataSource: PlantCatalogDataSource,
    private val generatePendingMessagesUseCase: GeneratePendingMessagesUseCase,
    private val messageFormatter: HomeOperationMessageFormatter,
) {
    suspend operator fun invoke(source: MessageSource = MessageSource.MANUAL_BUTTON): String {
        plantCatalogDataSource.refreshLocalSource()
        val summary = generatePendingMessagesUseCase(source = source)
        return messageFormatter.format(summary)
    }
}

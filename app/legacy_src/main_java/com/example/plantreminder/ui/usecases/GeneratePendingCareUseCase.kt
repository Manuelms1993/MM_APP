package com.example.plantreminder.ui.usecases

import com.example.plantreminder.data.repositories.PlantCatalogDataSource
import com.example.plantreminder.domain.models.MessageSource
import com.example.plantreminder.domain.usecases.GeneratePendingMessagesUseCase
import com.example.plantreminder.ui.HomeOperationMessageFormatter

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

package com.example.mmapp.app2.ui.usecases

import com.example.mmapp.app2.data.repositories.MenuCatalogDataSource
import com.example.mmapp.app2.domain.usecases.GeneratePendingMessagesUseCase
import com.example.mmapp.app2.ui.HomeOperationMessageFormatter
import java.time.LocalDate

class GeneratePendingMenuUseCase(
    private val menuCatalogDataSource: MenuCatalogDataSource,
    private val generatePendingMessagesUseCase: GeneratePendingMessagesUseCase,
    private val messageFormatter: HomeOperationMessageFormatter,
) {
    suspend operator fun invoke(
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusDays(7),
        silent: Boolean = false,
    ): String? {
        menuCatalogDataSource.refreshLocalSource()
        val summary = generatePendingMessagesUseCase(
            startDate = startDate,
            endDate = endDate,
        )
        return if (silent) null else messageFormatter.format(summary)
    }
}

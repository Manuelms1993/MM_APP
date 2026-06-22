package com.example.mmapp.app4

import android.app.Application
import com.example.mmapp.app4.data.centros.CentrosCsvAssetDataSource
import com.example.mmapp.app4.data.centros.CentrosCsvWriter
import com.example.mmapp.app4.data.centros.CentrosOsrmClient
import com.example.mmapp.app4.data.centros.CentrosProcessingService
import com.example.mmapp.app4.data.lacuponera.LacuponeraOfferDetailParser
import com.example.mmapp.app4.data.lacuponera.LacuponeraOffersClient
import com.example.mmapp.app4.data.lacuponera.LacuponeraFreeOffersParser
import com.example.mmapp.app4.domain.scripts.GenerateSchoolDriveTimesScript
import com.example.mmapp.app4.domain.scripts.FindFreeLacuponeraProductsScript
import com.example.mmapp.settings.data.repositories.AppSettingsRepository

class AppContainerFactory(
    private val application: Application,
) {
    fun create(
        appSettingsRepository: AppSettingsRepository,
    ): AppContainer = AppContainer(
        scripts = listOf(
            FindFreeLacuponeraProductsScript(
                client = LacuponeraOffersClient(
                    parser = LacuponeraFreeOffersParser(),
                    detailParser = LacuponeraOfferDetailParser(),
                ),
            ),
            GenerateSchoolDriveTimesScript(
                assetDataSource = CentrosCsvAssetDataSource(application),
                appSettingsRepository = appSettingsRepository,
                processingService = CentrosProcessingService(),
                osrmClient = CentrosOsrmClient(),
                csvWriter = CentrosCsvWriter(application),
            ),
        ),
    )
}

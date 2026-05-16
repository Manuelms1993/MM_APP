package com.example.mmapp.app3

import android.app.Application
import com.example.mmapp.app3.data.input.TravelGuideParser
import com.example.mmapp.app3.data.input.source.BundledTravelInputDataSource
import com.example.mmapp.app3.data.repositories.TravelGuideRepository

class AppContainerFactory(
    private val application: Application,
) {
    fun create(): AppContainer = AppContainer(
        travelGuideRepository = TravelGuideRepository(
            bundledTravelInputDataSource = BundledTravelInputDataSource(application),
            parser = TravelGuideParser(),
        ),
    )
}

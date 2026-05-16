package com.example.mmapp.app3.data.repositories

import com.example.mmapp.app3.data.input.TravelGuideParser
import com.example.mmapp.app3.data.input.source.BundledTravelInputDataSource
import com.example.mmapp.app3.domain.models.TravelGuide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TravelGuideRepository(
    private val bundledTravelInputDataSource: BundledTravelInputDataSource,
    private val parser: TravelGuideParser,
) {
    private val mutex = Mutex()

    @Volatile
    private var cachedGuide: TravelGuide? = null

    suspend fun getGuide(): TravelGuide = withContext(Dispatchers.IO) {
        cachedGuide ?: mutex.withLock {
            cachedGuide ?: loadGuide().also { cachedGuide = it }
        }
    }

    private fun loadGuide(): TravelGuide {
        val days = bundledTravelInputDataSource.loadDayInputs()
        val hotels = bundledTravelInputDataSource.loadHotelsInput()
        return parser.parse(days, hotels)
    }
}

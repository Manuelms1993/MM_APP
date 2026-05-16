package com.example.mmapp.app3.data.input.source

import android.content.Context
import com.example.mmapp.app3.data.input.RawTravelInput

class BundledTravelInputDataSource(
    private val context: Context,
) {
    fun loadDayInputs(): List<RawTravelInput> = context.assets.list("travel").orEmpty()
        .filter { it.endsWith(".json", ignoreCase = true) && it != HOTELS_FILE_NAME }
        .sorted()
        .mapNotNull { fileName -> read("travel/$fileName", fileName) }

    fun loadHotelsInput(): RawTravelInput? = read("travel/$HOTELS_FILE_NAME", HOTELS_FILE_NAME)

    private fun read(assetPath: String, fileName: String): RawTravelInput? = runCatching {
        RawTravelInput(
            fileName = fileName,
            rawJson = context.assets.open(assetPath).bufferedReader().use { it.readText() },
        )
    }.getOrNull()

    private companion object {
        private const val HOTELS_FILE_NAME = "hotels.json"
    }
}

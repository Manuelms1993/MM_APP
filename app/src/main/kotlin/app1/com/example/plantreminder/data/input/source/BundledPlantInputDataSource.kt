package com.example.mmapp.app1.data.input.source

import android.content.Context
import com.example.mmapp.app1.data.input.RawPlantInput

class BundledPlantInputDataSource(
    private val context: Context,
) {
    fun loadInputs(): List<RawPlantInput> {
        return context.assets.list("plants").orEmpty()
            .filter { it.endsWith(".json", ignoreCase = true) }
            .sorted()
            .mapNotNull { fileName -> readAssetFile(fileName, "plants/$fileName") }
    }

    private fun readAssetFile(fileName: String, assetPath: String = fileName): RawPlantInput? = runCatching {
        RawPlantInput(
            fileName = fileName,
            rawJson = context.assets.open(assetPath).bufferedReader().use { it.readText() },
        )
    }.getOrNull()
}

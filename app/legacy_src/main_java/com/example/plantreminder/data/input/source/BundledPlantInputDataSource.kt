package com.example.plantreminder.data.input.source

import android.content.Context
import com.example.plantreminder.data.input.RawPlantInput

class BundledPlantInputDataSource(
    private val context: Context,
) {
    fun loadInputs(): List<RawPlantInput> {
        val rootFiles = context.assets.list("").orEmpty()
            .filter { it.endsWith(".json", ignoreCase = true) }
            .sorted()

        if (rootFiles.isNotEmpty()) {
            return rootFiles.mapNotNull(::readAssetFile)
        }

        return context.assets.list("inputs").orEmpty()
            .filter { it.endsWith(".json", ignoreCase = true) }
            .sorted()
            .mapNotNull { fileName -> readAssetFile(fileName, "inputs/$fileName") }
    }

    private fun readAssetFile(fileName: String, assetPath: String = fileName): RawPlantInput? = runCatching {
        RawPlantInput(
            fileName = fileName,
            rawJson = context.assets.open(assetPath).bufferedReader().use { it.readText() },
        )
    }.getOrNull()
}

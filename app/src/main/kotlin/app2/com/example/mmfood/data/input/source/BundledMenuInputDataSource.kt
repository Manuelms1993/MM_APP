package com.example.mmapp.app2.data.input.source

import android.content.Context
import com.example.mmapp.app2.data.input.RawMenuInput

class BundledMenuInputDataSource(
    private val context: Context,
) {
    fun loadInputs(expectedFileNames: Set<String>): List<RawMenuInput> {
        return context.assets.list("food").orEmpty()
            .filter { it in expectedFileNames }
            .sorted()
            .mapNotNull { fileName -> readAssetFile(fileName, "food/$fileName") }
    }

    private fun readAssetFile(fileName: String, assetPath: String = fileName): RawMenuInput? = runCatching {
        RawMenuInput(
            fileName = fileName,
            rawJson = context.assets.open(assetPath).bufferedReader().use { it.readText() },
        )
    }.getOrNull()
}

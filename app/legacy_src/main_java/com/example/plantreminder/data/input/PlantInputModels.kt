package com.example.plantreminder.data.input

data class RawPlantInput(
    val fileName: String,
    val rawJson: String,
)

data class RawPlantLoadResult(
    val inputs: List<RawPlantInput>,
    val warningMessage: String? = null,
)

data class PlantSyncResult(
    val newCount: Int,
    val updatedCount: Int,
    val removedCount: Int,
    val unchangedCount: Int,
)

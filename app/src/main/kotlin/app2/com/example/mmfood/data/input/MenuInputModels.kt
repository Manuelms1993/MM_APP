package com.example.mmapp.app2.data.input

data class RawMenuInput(
    val fileName: String,
    val rawJson: String,
)

data class RawMenuLoadResult(
    val inputs: List<RawMenuInput>,
    val warningMessage: String? = null,
)

data class MenuSyncResult(
    val newCount: Int,
    val updatedCount: Int,
    val removedCount: Int,
    val unchangedCount: Int,
)

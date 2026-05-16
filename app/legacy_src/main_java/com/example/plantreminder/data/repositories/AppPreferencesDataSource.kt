package com.example.plantreminder.data.repositories

import kotlinx.coroutines.flow.Flow

interface AppPreferencesDataSource {
    fun observeActiveMaintainer(): Flow<String>

    suspend fun getActiveMaintainer(): String

    suspend fun setActiveMaintainer(maintainer: String)
}

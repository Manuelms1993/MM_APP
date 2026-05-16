package com.example.mmapp.app1.data.repositories

import kotlinx.coroutines.flow.Flow

interface AppPreferencesDataSource {
    fun observeActiveMaintainer(): Flow<String>

    suspend fun getActiveMaintainer(): String

    suspend fun setActiveMaintainer(maintainer: String)
}

package com.example.plantreminder.data.repositories

import com.example.plantreminder.data.db.AppPreferencesDao
import com.example.plantreminder.data.db.AppPreferencesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferencesRepository(
    private val dao: AppPreferencesDao,
) : AppPreferencesDataSource {
    override fun observeActiveMaintainer(): Flow<String> = dao.observe().map { entity ->
        entity?.activeMaintainer?.normalizeMaintainer() ?: AppPreferencesEntity.DEFAULT_MAINTAINER
    }

    override suspend fun getActiveMaintainer(): String =
        dao.get()?.activeMaintainer?.normalizeMaintainer() ?: AppPreferencesEntity.DEFAULT_MAINTAINER

    override suspend fun setActiveMaintainer(maintainer: String) {
        dao.upsert(
            AppPreferencesEntity(
                activeMaintainer = maintainer.normalizeMaintainer(),
            ),
        )
    }

    private fun String.normalizeMaintainer(): String = uppercase().takeIf { it == "L" || it == "R" }
        ?: AppPreferencesEntity.DEFAULT_MAINTAINER
}

package com.example.mmapp.app1.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferencesEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val activeMaintainer: String = DEFAULT_MAINTAINER,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
        const val DEFAULT_MAINTAINER = "L"
    }
}

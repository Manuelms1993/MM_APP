package com.example.mmapp.settings

import android.content.Context
import com.example.mmapp.app4.work.ProcessScheduler
import com.example.mmapp.settings.data.repositories.ProcessSettings

class ProcessSettingsCoordinator(
    private val context: Context,
) {
    fun apply(settings: List<ProcessSettings>) {
        ProcessScheduler(context).sync(settings)
    }
}

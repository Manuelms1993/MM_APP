package com.example.mmapp.app4.domain.scripts

import com.example.mmapp.app4.domain.models.ScriptDefinition
import com.example.mmapp.app4.domain.models.ScriptExecutionResult

interface ScriptTask {
    val definition: ScriptDefinition

    suspend fun execute(): ScriptExecutionResult
}

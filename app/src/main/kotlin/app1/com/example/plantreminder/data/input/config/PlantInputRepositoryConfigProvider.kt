package com.example.mmapp.app1.data.input.config

import com.example.mmapp.BuildConfig

interface PlantInputRepositoryConfigProvider {
    fun get(): PlantInputRepositoryConfig
}

class BuildConfigPlantInputRepositoryConfigProvider : PlantInputRepositoryConfigProvider {
    override fun get(): PlantInputRepositoryConfig = PlantInputRepositoryConfig(
        inputsRepositoryTreeUrl = BuildConfig.PLANTS_INPUTS_REPOSITORY_TREE_URL,
    )
}

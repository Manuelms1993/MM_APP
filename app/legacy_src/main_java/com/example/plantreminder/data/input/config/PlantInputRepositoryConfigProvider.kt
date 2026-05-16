package com.example.plantreminder.data.input.config

import com.example.plantreminder.BuildConfig

interface PlantInputRepositoryConfigProvider {
    fun get(): PlantInputRepositoryConfig
}

class BuildConfigPlantInputRepositoryConfigProvider : PlantInputRepositoryConfigProvider {
    override fun get(): PlantInputRepositoryConfig = PlantInputRepositoryConfig(
        inputsRepositoryTreeUrl = BuildConfig.INPUTS_REPOSITORY_TREE_URL,
    )
}

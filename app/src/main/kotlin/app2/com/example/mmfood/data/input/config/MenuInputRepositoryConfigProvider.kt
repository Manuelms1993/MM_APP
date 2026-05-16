package com.example.mmapp.app2.data.input.config

import com.example.mmapp.BuildConfig

interface MenuInputRepositoryConfigProvider {
    fun get(): MenuInputRepositoryConfig
}

class BuildConfigMenuInputRepositoryConfigProvider : MenuInputRepositoryConfigProvider {
    override fun get(): MenuInputRepositoryConfig = MenuInputRepositoryConfig(
        inputsRepositoryTreeUrl = BuildConfig.FOOD_INPUTS_REPOSITORY_TREE_URL,
        expectedFileNames = setOf("comidas.json", "cenas.json"),
    )
}

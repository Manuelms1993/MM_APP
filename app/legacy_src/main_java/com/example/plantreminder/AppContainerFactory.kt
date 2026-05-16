package com.example.plantreminder

import android.app.Application
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.plantreminder.data.db.AppDatabase
import com.example.plantreminder.data.input.FlexibleTaskParser
import com.example.plantreminder.data.input.config.BuildConfigPlantInputRepositoryConfigProvider
import com.example.plantreminder.data.input.source.BundledPlantInputDataSource
import com.example.plantreminder.data.input.source.GithubPlantInputDataSource
import com.example.plantreminder.data.input.source.PlantInputCacheStore
import com.example.plantreminder.data.input.source.PlantInputLoader
import com.example.plantreminder.data.input.source.PlantInputSyncService
import com.example.plantreminder.data.input.validation.PlantCatalogValidator
import com.example.plantreminder.data.input.validation.PlantDefinitionValidator
import com.example.plantreminder.data.repositories.AppPreferencesRepository
import com.example.plantreminder.data.repositories.ConversationRepository
import com.example.plantreminder.data.repositories.PlantDefinitionRepository
import com.example.plantreminder.data.repositories.WeatherRepository
import com.example.plantreminder.data.weather.OpenMeteoWeatherService
import com.example.plantreminder.domain.DailyActionCalculator
import com.example.plantreminder.domain.MessageBuilder
import com.example.plantreminder.domain.PlantActionTextFactory
import com.example.plantreminder.domain.usecases.GetMaintainerPendingActionsUseCase
import com.example.plantreminder.domain.usecases.GenerateDailyMessageUseCase
import com.example.plantreminder.domain.usecases.GeneratePendingMessagesUseCase
import com.example.plantreminder.ui.HomeOperationMessageFormatter
import com.example.plantreminder.ui.usecases.GeneratePendingCareUseCase
import com.example.plantreminder.ui.usecases.LoadPlantsUseCase
import com.example.plantreminder.ui.usecases.ObserveActiveMaintainerUseCase
import com.example.plantreminder.ui.usecases.ObserveConversationUseCase
import com.example.plantreminder.ui.usecases.ObservePlantLoadWarningsUseCase
import com.example.plantreminder.ui.usecases.ObserveWeatherUseCase
import com.example.plantreminder.ui.usecases.RefreshWeatherUseCase
import com.example.plantreminder.ui.usecases.SetActiveMaintainerUseCase
import com.example.plantreminder.ui.usecases.SyncPlantInputsUseCase

class AppContainerFactory(
    private val application: Application,
) {
    fun create(): AppContainer {
        val database = Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "plant-reminder.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

        val plantCatalogDataSource = createPlantCatalogDataSource()
        val conversationDataSource = ConversationRepository(database.conversationMessageDao())
        val weatherDataSource = WeatherRepository(
            weatherDayDao = database.weatherDayDao(),
            weatherService = OpenMeteoWeatherService(),
        )
        val appPreferencesDataSource = AppPreferencesRepository(database.appPreferencesDao())
        val dailyActionCalculator = DailyActionCalculator(PlantActionTextFactory())
        val generateDailyMessageUseCase = GenerateDailyMessageUseCase(
            plantDefinitionRepository = plantCatalogDataSource,
            conversationRepository = conversationDataSource,
            weatherDataSource = weatherDataSource,
            dailyActionCalculator = dailyActionCalculator,
            messageBuilder = MessageBuilder(),
        )
        val generatePendingMessagesUseCase = GeneratePendingMessagesUseCase(
            conversationRepository = conversationDataSource,
            plantDefinitionRepository = plantCatalogDataSource,
            generateDailyMessageUseCase = generateDailyMessageUseCase,
        )
        val messageFormatter = HomeOperationMessageFormatter()

        return AppContainer(
            observeConversationUseCase = ObserveConversationUseCase(conversationDataSource),
            loadPlantsUseCase = LoadPlantsUseCase(plantCatalogDataSource),
            observePlantLoadWarningsUseCase = ObservePlantLoadWarningsUseCase(plantCatalogDataSource),
            generatePendingCareUseCase = GeneratePendingCareUseCase(
                plantCatalogDataSource = plantCatalogDataSource,
                generatePendingMessagesUseCase = generatePendingMessagesUseCase,
                messageFormatter = messageFormatter,
            ),
            syncPlantInputsUseCase = SyncPlantInputsUseCase(
                plantCatalogDataSource = plantCatalogDataSource,
                messageFormatter = messageFormatter,
            ),
            observeWeatherUseCase = ObserveWeatherUseCase(weatherDataSource),
            refreshWeatherUseCase = RefreshWeatherUseCase(weatherDataSource),
            observeActiveMaintainerUseCase = ObserveActiveMaintainerUseCase(appPreferencesDataSource),
            setActiveMaintainerUseCase = SetActiveMaintainerUseCase(appPreferencesDataSource),
            plantCatalogDataSource = plantCatalogDataSource,
            conversationDataSource = conversationDataSource,
            weatherDataSource = weatherDataSource,
            appPreferencesDataSource = appPreferencesDataSource,
            getMaintainerPendingActionsUseCase = GetMaintainerPendingActionsUseCase(
                plantDefinitionRepository = plantCatalogDataSource,
                weatherDataSource = weatherDataSource,
                dailyActionCalculator = dailyActionCalculator,
            ),
        )
    }

    private fun createPlantCatalogDataSource(): PlantDefinitionRepository {
        val config = BuildConfigPlantInputRepositoryConfigProvider().get()
        val cacheStore = PlantInputCacheStore(application)
        val inputLoader = PlantInputLoader(
            bundledDataSource = BundledPlantInputDataSource(application),
            cacheStore = cacheStore,
        )
        val inputSyncService = PlantInputSyncService(
            remoteDataSource = GithubPlantInputDataSource(config),
            cacheStore = cacheStore,
        )
        return PlantDefinitionRepository(
            plantInputLoader = inputLoader,
            plantInputSyncService = inputSyncService,
            plantCatalogValidator = PlantCatalogValidator(
                plantDefinitionFactory = FlexibleTaskParser(),
                plantDefinitionValidator = PlantDefinitionValidator(),
            ),
        )
    }

    private companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weather_days` (
                        `date` TEXT NOT NULL,
                        `locationName` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `rainMm` REAL NOT NULL,
                        `precipitationMm` REAL NOT NULL,
                        `precipitationProbabilityMax` INTEGER,
                        `precipitationHours` REAL NOT NULL,
                        `provider` TEXT NOT NULL,
                        `fetchedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`date`)
                    )
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_preferences` (
                        `id` INTEGER NOT NULL,
                        `activeMaintainer` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `app_preferences` (`id`, `activeMaintainer`)
                    VALUES (1, 'L')
                    """.trimIndent(),
                )
            }
        }
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.mmapp"
    compileSdk = 34

    val plantsInputsRepositoryTreeUrl = providers.gradleProperty("plantsInputsRepositoryTreeUrl")
        .orElse("https://github.com/example/mm-app-inputs/tree/main/inputs/plants")
        .get()
    val foodInputsRepositoryTreeUrl = providers.gradleProperty("foodInputsRepositoryTreeUrl")
        .orElse("https://github.com/example/mm-app-inputs/tree/main/inputs/food")
        .get()

    val signingPropertiesFile = rootProject.file("signing/release-keystore.properties")
    val signingProperties = Properties().apply {
        if (signingPropertiesFile.exists()) {
            signingPropertiesFile.inputStream().use(::load)
        }
    }

    val releaseKeystoreFile = System.getenv("ANDROID_KEYSTORE_FILE")
        ?: signingProperties.getProperty("storeFile")
            ?.takeIf { it.isNotBlank() }
            ?.let(rootProject::file)
            ?.path
    val hasReleaseSigning = !releaseKeystoreFile.isNullOrBlank()

    defaultConfig {
        applicationId = "com.example.mmapp"
        minSdk = 26
        targetSdk = 34
        versionCode = providers.gradleProperty("appVersionCode")
            .orElse("1")
            .map(String::toInt)
            .get()
        versionName = providers.gradleProperty("appVersionName")
            .orElse("1.0.0")
            .get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "PLANTS_INPUTS_REPOSITORY_TREE_URL",
            "\"$plantsInputsRepositoryTreeUrl\"",
        )
        buildConfigField(
            "String",
            "FOOD_INPUTS_REPOSITORY_TREE_URL",
            "\"$foodInputsRepositoryTreeUrl\"",
        )
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystoreFile)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    ?: signingProperties.getProperty("storePassword")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                    ?: signingProperties.getProperty("keyAlias")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
                    ?: signingProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            assets.setSrcDirs(listOf(rootProject.file("inputs").path))
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.4.2")

    debugImplementation(composeBom)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.huanchengfly.tieba.core.testing"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:mvi"))
    api(project(":core:network"))
    api(project(":core:ui"))
    api(project(":data:repository"))

    api(libs.junit.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.turbine)
    api(kotlin("test"))
}

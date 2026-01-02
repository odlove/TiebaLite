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
    api(project(":core:ui:foundation"))
    api(project(":core:ui:device"))
    api(project(":core:ui:view"))
    api(project(":core:ui:compose-base"))
    api(project(":core:ui:compose-widgets"))
    api(project(":core:ui:media"))
    api(project(":core:ui:navigation"))
    api(project(":core:ui:app"))
    api(project(":data:repository"))

    api(libs.junit.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
    api(libs.turbine)
    api(kotlin("test"))
}

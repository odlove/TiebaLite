plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.wire)
}

android {
    namespace = "com.huanchengfly.tieba.core.network"
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

    buildFeatures {
        buildConfig = false
    }
}

wire {
    sourcePath {
        srcDir("src/main/proto")
    }

    kotlin {
        android = true
    }
}

dependencies {
    implementation(project(":core:common"))

    api(libs.okhttp3.core)
    api(libs.retrofit2.core)
    api(libs.retrofit2.converter.wire)
    api(libs.wire.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.gson)
    implementation(libs.javax.inject)
}

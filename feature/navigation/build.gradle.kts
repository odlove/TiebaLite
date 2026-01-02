plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.huanchengfly.tieba.feature.navigation"
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
        compose = true
        buildConfig = false
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:runtime"))
    implementation(project(":core:ui:theme"))
    implementation(project(":core:ui:app"))
    implementation(project(":core:ui:foundation"))
    implementation(project(":core:ui:device"))
    implementation(project(":core:ui:compose-widgets"))
    implementation(project(":core:ui:media"))
    implementation(project(":core:ui:navigation"))
    implementation(project(":core:mvi"))

    implementation(project(":feature:home"))
    implementation(project(":feature:webview"))
    implementation(project(":feature:photoview"))
    implementation(project(":feature:threadcollect"))
    implementation(project(":feature:subposts"))
    implementation(project(":feature:reply"))
    implementation(project(":feature:thread"))
    implementation(project(":feature:history"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:login"))
    implementation(project(":feature:hottopic"))
    implementation(project(":feature:search"))
    implementation(project(":feature:forum"))
    implementation(project(":feature:user"))
    implementation(project(":feature:sign"))
    implementation(project(":feature:clipboard"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)
}

ksp {
    arg("compose-destinations.mode", "singlemodule")
    arg("compose-destinations.moduleName", "navigation")
}

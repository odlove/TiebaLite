plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.huanchengfly.tieba.feature.settings"
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
        viewBinding = true
        buildConfig = false
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:mvi"))
    implementation(project(":core:network"))
    implementation(project(":core:runtime"))
    implementation(project(":core:ui:theme"))
    implementation(project(":core:ui:foundation"))
    implementation(project(":core:ui:device"))
    implementation(project(":core:ui:view"))
    implementation(project(":core:ui:compose-base"))
    implementation(project(":core:ui:compose-widgets"))
    implementation(project(":core:ui:navigation"))
    implementation(project(":core:ui:app"))
    implementation(project(":data:repository"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.javax.inject)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.kotlin.reflect)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.palette)
    implementation(libs.org.litepal.android.kotlin)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.eygraber.placeholder.material)
    implementation(libs.com.gyf.immersionbar.immersionbar)
    implementation(libs.google.material)
    implementation(libs.com.jaredrummler.colorpicker)
    implementation(libs.godaddy.color.picker)
    implementation(libs.com.github.yalantis.ucrop)
    implementation(libs.systemuibars.tweaker)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.google.gson)
    implementation("androidx.preference:preference:1.2.1")
    implementation(libs.sketch.core)
    implementation(libs.sketch.compose)
    implementation(libs.sketch.ext.compose)
    implementation(libs.sketch.gif)
    implementation(libs.sketch.okhttp)
    implementation("io.github.panpf.sketch3:sketch-extensions-core:${libs.versions.sketch.get()}")

    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)
}

ksp {
    arg("compose-destinations.mode", "destinations")
    arg("compose-destinations.moduleName", "settings")
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.huanchengfly.tieba.core.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    lint {
        baseline = file("lint-baseline.xml")
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
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:mvi"))
    implementation(project(":core:common"))
    implementation(project(":core:runtime"))
    implementation(project(":core:network"))

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.javax.inject)
    implementation(libs.systemuibars.tweaker)
    implementation(libs.org.litepal.android.kotlin)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)
    implementation(libs.compose.animation.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.window)
    implementation(libs.compose.destinations.core)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.core)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.sketch.core)
    implementation(libs.sketch.compose)
    implementation(libs.sketch.ext.compose)
    implementation(libs.sketch.gif)
    implementation(libs.sketch.okhttp)
    implementation("io.github.panpf.sketch3:sketch-extensions-core:${libs.versions.sketch.get()}")
    implementation(libs.com.gyf.immersionbar.immersionbar)
    implementation(libs.github.matisse)
    implementation(libs.glide.core)
    implementation(libs.glide.okhttp3.integration)
    implementation(libs.google.gson)
    implementation(libs.airbnb.lottie.compose)
    implementation(libs.eygraber.placeholder.material)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.xx.permissions)
}

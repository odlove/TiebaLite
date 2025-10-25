// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.wire) apply false

    // Dependency Analysis plugin only supports AGP 8.0.0-8.4.0, commented out for AGP 8.13.0
    // alias(libs.plugins.dependency.analysis)
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory.asFile.get())
}

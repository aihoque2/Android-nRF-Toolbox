
plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "no.nordicsemi.android.feature.profile"
}

dependencies {
    implementation(project(":profile_hts"))

    implementation(project(":lib_analytics"))
    implementation(project(":lib_service"))
    implementation(project(":lib_scanner"))
    implementation(project(":lib_ui"))
    implementation(project(":lib_utils"))

    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.profile)

    implementation(libs.nordic.ble.common)
    implementation(libs.nordic.ble.ktx)

    implementation(libs.nordic.theme)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.uilogger)
    implementation(libs.nordic.core)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.service)
}

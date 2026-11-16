plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.al9yser.mufakkik"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.al9yser.mufakkik"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    packaging {
        resources {
            excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1", "META-INF/LICENSE*", "META-INF/NOTICE*")
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Real Dalvik bytecode parsing and Smali disassembly.
    implementation("org.smali:dexlib2:2.5.2")
    implementation("org.smali:baksmali:2.5.2")
    implementation("com.google.guava:guava:33.4.0-android")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

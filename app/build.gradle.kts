plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.zynt.sumviltadconnect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zynt.sumviltadconnect"
        minSdk = 24
        targetSdk = 36
        
        // App Versioning - Follow Semantic Versioning (MAJOR.MINOR.PATCH)
        // versionCode must be incremented for each release to Play Store/GitHub
        // versionName is the user-visible version string
        versionCode = 120         // Increment by 1 for each release
        versionName = "1.2.0"     // Update following SemVer: MAJOR.MINOR.PATCH

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build configuration fields for runtime access
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
        buildConfigField("int", "VERSION_CODE", "${versionCode}")
        buildConfigField("String", "GITHUB_REPO", "\"Vincentjhon31/SUMVILTAD\"")
        buildConfigField("String", "RELEASE_URL", "\"https://github.com/Vincentjhon31/SUMVILTAD/releases\"")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("sumviltad-release.keystore")
            storePassword = "sumviltad2025"
            keyAlias = "sumviltad"
            keyPassword = "sumviltad2025"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use proper release signing for consistent updates
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Enable BuildConfig generation
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    androidResources {
        noCompress += "tflite"
        noCompress += "pt"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // PyTorch
    implementation("org.pytorch:pytorch_android:1.13.1")
    implementation("org.pytorch:pytorch_android_torchvision:1.13.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.2")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Compose BOM (manages all Compose versions)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // Core Compose libs (no versions!)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // EXIF data handling for image orientation
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // CardView for custom marker
    implementation("androidx.cardview:cardview:1.0.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Accompanist utilities
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.32.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Charts
    implementation("com.patrykandpatrick.vico:core:2.0.0")
    implementation("com.patrykandpatrick.vico:compose:2.0.0")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.firebase.inappmessaging.display)

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


}

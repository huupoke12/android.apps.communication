plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
}

android {
    namespace = "io.github.huupoke12.android.apps.communication"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.huupoke12.android.apps.communication"
        minSdk = 24
        targetSdk = 33 // Jitsi SDK not work at 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3
    implementation("androidx.compose.material3:material3")

    // Optional - Add full set of material icons
    // +10MB app size
    implementation("androidx.compose.material:material-icons-extended")

    // Optional - Integration with LiveData
    implementation("androidx.compose.runtime:runtime-livedata")

    // Compose additions
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    //    implementation("com.google.accompanist:accompanist-permissions:1.5.2")

    // Image loader
    val coilBom = platform("io.coil-kt:coil-bom:2.5.0")
    implementation(coilBom)
    implementation("io.coil-kt:coil-compose")
    implementation("io.coil-kt:coil-video")

    // Media player
    val exoPlayerVersion = "1.2.0"
    implementation("androidx.media3:media3-exoplayer:$exoPlayerVersion")
    implementation("androidx.media3:media3-ui:$exoPlayerVersion")

    // Matrix SDK
    implementation("org.matrix.android:matrix-android-sdk2:1.5.30")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jitsi SDK
    implementation("org.jitsi.react:jitsi-meet-sdk:8.6.0")

/*    val jjwtVersion = "0.12.2"
    api("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-orgjson:$jjwtVersion") {
        exclude(group = "org.json", module = "json") // provided by Android natively
    }*/

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1") // Downgrade to 0.4.0 if error
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    implementation("androidx.startup:startup-runtime:1.1.1")
}
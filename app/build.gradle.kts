plugins {
    id("com.android.application")
}

android {
    namespace = "com.lbxq.screen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lbxq.screen"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    lint {
        abortOnError = false
        ignoreWarnings = true
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("de.robv.android.xposed:api:82")
    implementation("de.robv.android.xposed:api-static:82")
}

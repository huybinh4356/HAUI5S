plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.haui5s"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.haui5s"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.applandeo:material-calendar-view:1.9.2")
    implementation("androidx.activity:activity:1.9.3")
    implementation("mysql:mysql-connector-java:5.1.49")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.haui5s"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.haui5s"
        minSdk = 26
        targetSdk = 35
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
    implementation("mysql:mysql-connector-java:5.1.49")
    implementation(libs.appcompat)
    implementation(libs.material)

    implementation("androidx.activity:activity:1.9.3")

    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.applandeo:material-calendar-view:1.9.2")
}
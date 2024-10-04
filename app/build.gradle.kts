plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.infinityfitness"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.infinityfitness"
        minSdk = 28
        targetSdk = 34
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

    defaultConfig {
        applicationId = "com.example.infinityfitness" // Ensure this matches the authority in the manifest
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

//    kotlin {
//        jvmToolchain(8) // Ensure KSP and Kotlin use the same JVM version
//    }
}
dependencies {
// Google Places SDK
    implementation ("com.google.android.libraries.places:places:2.6.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.activity:activity:1.9.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.itextpdf:itext7-core:7.1.14")

    //room dependencies
    val room_version = "2.5.2"
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    ksp("com.github.bumptech.glide:compiler:4.12.0")

    // view model dependencies
    val viewmodelversion = "2.6.1"
    //noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$viewmodelversion")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")


}
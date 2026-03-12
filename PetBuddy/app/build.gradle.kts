plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.petbuddy"
    compileSdk {
        version = release(36)
    }
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.petbuddy"
        minSdk = 29
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
    kotlinOptions {
        jvmTarget = "11"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        //calendar
        isCoreLibraryDesugaringEnabled = true

    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("ru.cleverpumpkin:crunchycalendar:2.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")

    implementation("io.ktor:ktor-client-okhttp:2.3.7")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ViewModel และ LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // สำหรับ by viewModels() ใน Fragment
    implementation("androidx.activity:activity-ktx:1.8.0")

    implementation("com.google.firebase:firebase-messaging:23.4.1")

    //calendar
    implementation("com.kizitonwose.calendar:view:2.9.0")
}
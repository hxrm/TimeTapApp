plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)


}

android {

    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.timetapwebapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.timetapwebapp"
        minSdk = 24
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {


    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-database")
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.room.common)
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation("com.github.Philjay:mpandroidchart:3.1.0")
    implementation("androidx.room:room-runtime:2.4.1")

    implementation("androidx.room:room-ktx:2.4.1")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("com.squareup.picasso:picasso:2.8")







    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    implementation(libs.androidx.appcompat)
    // For loading and tinting drawables on older versions of the platform
    implementation(libs.androidx.appcompat.resources)

}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
    id("kotlin-parcelize")
}

android {
    namespace = "pl.pb.optigai"
    compileSdk = 36

    defaultConfig {
        applicationId = "pl.pb.optigai"
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
                "proguard-rules.pro",
            )
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.glide)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.quickbirdstudios:opencv:4.5.2")
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.datastore.proto)
    implementation(libs.protobuf.javalite)
    implementation("org.tensorflow:tensorflow-lite:2.15.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.constraintlayout.core)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.0"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { option("lite") }
            }
        }
    }
}

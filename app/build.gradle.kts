plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.yxqyrh.janusandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yxqyrh.janusandroid"
        minSdk = 24
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
    packaging {
        resources {
            excludes.addAll(arrayOf("META-INF/INDEX.LIST", "META-INF/io.netty.versions.properties"))
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.extesions)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.reactivestreams.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.recycleview)
    implementation(libs.androidx.multidex)
//    implementation(libs.navigation.fragment.ktx)
//    implementation(libs.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.datastore.preferences.rxjava3)

    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava3)
    implementation(libs.androidx.room.testing)
    ksp(libs.androidx.room.comile)
    implementation(libs.sqite.jdbc)

    implementation(libs.squareup.okhttp3)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.convertgson)
    implementation(libs.jakewharton.retrofit)
    implementation(libs.squareup.moshi)
    implementation(libs.squareup.moshi.kotlin)
    implementation(libs.squareup.moshi.adapters)
    ksp(libs.squareup.moshi.kotlin.codegen)

    implementation(libs.tencent.bugly)
    implementation(libs.webrtc)
    implementation(libs.java.websocket)
    implementation(libs.tinder.scarlet)
    implementation(libs.tinder.scarlet.websocket.okhttp)
    implementation(libs.tinder.scarlet.lifecycle.android)
    implementation(libs.tinder.scarlet.stream.adapter.rxjava2)

    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxpermission)
    implementation(libs.baseRecycleviewAdapter)
    implementation(libs.superBottomSheet)

}
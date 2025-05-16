plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    //di
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.google.gms.google.services)
}

/*ksp {
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
    arg("dagger.hilt.internal.useAggregatingRootProcessor", "true")
    arg("dagger.fastInit", "enabled")
    arg("dagger.hilt.android.internal.projectType", "APPLICATION")
}*/
android {
    namespace = "com.live.humanmesh"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.live.humanmesh"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}


dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // AndroidX Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Google Play Services
    implementation(libs.play.services.fitness)
    implementation(libs.play.services.maps.v1700)
    implementation(libs.play.services.location)
    implementation(libs.play.services.places)
    implementation(libs.gms.play.services.maps)

    // Network & Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.adapter.rxjava2)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)

    // Image Loading
    implementation(libs.coil)
    implementation(libs.glide)
//    implementation(libs.firebase.messaging)
    annotationProcessor(libs.compiler)
    implementation(libs.glide.transformations)

    // UI Libraries
    implementation(libs.circleindicator)
    implementation(libs.circleimageview)
    implementation(libs.roundedimageview)
    implementation(libs.dotsindicator)
    implementation("com.github.zhpanvip:viewpagerindicator:1.2.2")
    implementation(libs.android.otpview.pinview.otpview)
    implementation(libs.pinview)
    implementation(libs.stickyscrollview)
    implementation(libs.alerter)
    implementation(libs.android.simple.tooltip)
    implementation(libs.blurry)

    // Calendar Views
    implementation(libs.material.calendar.view)
    implementation(libs.material.calendarview)

    // Permissions
//    implementation(libs.dexter)

    // Location and Places
    implementation(libs.places)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.android.v173)

    // Responsive UI
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Misc
    implementation(libs.androidx.connect.client)
    implementation(libs.ui)
    implementation(libs.androidx.runtime)

    // Country Code Picker (deduplicated)
    implementation(libs.ccp)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //socket
    //Socket.io v2
    /* implementation("io.socket:socket.io-client:1.0.0") {
         exclude(group = "org.json", module = "json")
     }*/
//    Socket.io v4
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("id.zelory:compressor:3.0.1")
    implementation("com.vanniktech:android-image-cropper:4.6.0")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("com.karumi:dexter:6.2.3")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("me.leolin:ShortcutBadger:1.1.22@aar")
    implementation("com.jsibbold:zoomage:1.3.1")
    //google login
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-ads:23.6.0")

}

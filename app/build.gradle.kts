import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}


android {
    namespace = "me.seasonyuu.xperiatools"
    compileSdk = 33

    defaultConfig {
        applicationId = "me.seasonyuu.xperiatools"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    signingConfigs {
        val properties = Properties()
        val input = project.rootProject.file("local.properties").inputStream()
        properties.load(input)

        val appStorePassword = properties.getProperty("STORE_PASSWORD")
        val appKeyAlias = properties.getProperty("KEY_ALIAS")
        val appKeyPassword = properties.getProperty("KEY_PASSWORD")

        getByName("debug") {
            storeFile = file("./signing.jks")
            keyAlias = appKeyAlias
            keyPassword = appKeyPassword
            storePassword = appStorePassword
        }
        create("release") {
            storeFile = file("./signing.jks")
            keyAlias = appKeyAlias
            keyPassword = appKeyPassword
            storePassword = appStorePassword
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    compileOnly(libs.xposed.api)
    implementation(libs.yukihookapi)
    ksp(libs.yukihookapi.ksp)
}
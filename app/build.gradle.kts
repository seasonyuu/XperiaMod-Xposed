import java.util.Properties
import org.apache.commons.io.output.ByteArrayOutputStream

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}

fun git(vararg args: String): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine = args.asList().let { argsList ->
            arrayListOf("git").also { it.addAll(argsList) }
        }
        standardOutput = output
    }
    return output.toString().trim()
}

val gitHash = git("rev-parse", "--short", "HEAD")
val gitCommits = git("rev-list", "--count", "HEAD")
val gitDescribe = git("describe", "--tags")
val version = gitDescribe.ifEmpty {
    gitHash
}

android {
    namespace = "me.seasonyuu.xperiatools"
    compileSdk = 33

    defaultConfig {
        applicationId = "me.seasonyuu.xperiatools"
        minSdk = 24
        targetSdk = 33
        versionCode = if (gitCommits.isNotEmpty()) gitCommits.toInt() else 1
        versionName = version

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

configurations.configureEach {
    exclude("androidx.appcompat", "appcompat")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.preference)
    implementation(libs.color.picker.preference)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.rikka.appcompat)
    implementation(libs.rikka.core.ktx)
    implementation(libs.rikka.compatibility)
    implementation(libs.rikka.material)
    implementation(libs.rikka.material.preference)
    implementation(libs.rikka.preference.simplemenu)

    compileOnly(libs.xposed.api)
    implementation(libs.yukihookapi)
    ksp(libs.yukihookapi.ksp)
}
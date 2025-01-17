// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
buildscript {
    repositories {
        google() // Google Maven repository
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath ("com.google.gms:google-services:4.4.2") // Google Services eklentisi
        classpath("com.android.tools.build:gradle:7.4.2") // En güncel gradle versiyonunu kullanın
        classpath("com.google.gms:google-services:4.3.15") // En güncel google-services versiyonunu kullanın
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
    }
}



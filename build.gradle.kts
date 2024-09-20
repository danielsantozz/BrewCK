plugins {
    id("com.android.application") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2") // Atualize para a versão correta
        classpath("com.google.gms:google-services:4.3.15") // Atualize para a versão correta
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

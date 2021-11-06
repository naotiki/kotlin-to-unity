val kspVersion: String by project

plugins {
    kotlin("jvm")
}

group = "xyz.naotiki_apps"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup:javapoet:1.12.1")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}




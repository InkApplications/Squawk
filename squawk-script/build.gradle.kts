plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.coroutines.core)
}

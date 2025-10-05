plugins {
    `kotlin-dsl`
}
repositories {
    gradlePluginPortal()
    mavenCentral()
}
dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.serialization.gradle)
}

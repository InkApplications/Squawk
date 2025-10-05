plugins {
    application
    kotlin("jvm")
}

application {
    applicationName = "squawk"
    mainClass.set("squawk.cli.MainKt")
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.clikt)
}

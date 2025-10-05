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
    implementation(libs.bundles.ktor.client)
    implementation(projects.squawkScript)
    implementation("org.slf4j:slf4j-nop:2.0.17")
}

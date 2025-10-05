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
    implementation(libs.kotlin.scripting.jvm.core)
    implementation(libs.kotlin.scripting.jvm.host)
    implementation(libs.bundles.ktor.client)
    implementation("org.slf4j:slf4j-nop:2.0.17")
}

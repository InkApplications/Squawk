plugins {
    kotlin("jvm")
}

dependencies {
    api(projects.squawkScript)
    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm.host)
}

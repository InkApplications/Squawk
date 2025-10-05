import org.gradle.api.tasks.testing.logging.TestExceptionFormat

allprojects {
    repositories {
        mavenCentral()
    }
    tasks.withType(Test::class) {
        testLogging.exceptionFormat = TestExceptionFormat.FULL
    }
}

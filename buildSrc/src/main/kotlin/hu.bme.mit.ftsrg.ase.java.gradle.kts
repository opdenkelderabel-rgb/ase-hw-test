import org.gradle.accessors.dm.LibrariesForLibs
plugins {
    java
    jacoco
}

java{
    toolchain{
        languageVersion=JavaLanguageVersion.of(25)
    }
}

repositories{
    mavenCentral()
}

val libs = the<LibrariesForLibs>()
dependencies{
    testRuntimeOnly(libs.junit.platform.launcher)
    
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    finalizedBy(tasks.jacocoTestReport)
}
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.plugin.node)
    implementation(libs.jinjava)
    implementation(libs.jackson.databind)
}

gradlePlugin {
    plugins {
        register("codegen") {
            id = "hu.bme.mit.ftsrg.ase.codegen"
            implementationClass = "hu.bme.mit.ftsrg.ase.gradle.codegen.CodegenPlugin"
        }
        register("regexGenerator") {
            id = "hu.bme.mit.ftsrg.ase.regex-generator"
            implementationClass = "hu.bme.mit.ftsrg.ase.gradle.regexgenerator.RegexGeneratorPlugin"
        }
    }
}

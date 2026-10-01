import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.github.node-gradle.node")
}

val libs = the<LibrariesForLibs>()

node {
    download = true
    version = libs.versions.node.get()
}

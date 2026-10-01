import com.github.gradle.node.npm.task.NpmTask
import hu.bme.mit.ftsrg.ase.gradle.codegen.CodegenPlugin

plugins {
    base
    id("hu.bme.mit.ftsrg.ase.node")
}

node {
    npmInstallCommand = "ci"
}

val grammar = layout.projectDirectory.file("src/regex.langium")
val packageFile = layout.projectDirectory.file("package.json")
val lockFile = layout.projectDirectory.file("package-lock.json")
val sources = layout.projectDirectory.dir("src")
val tests = layout.projectDirectory.dir("test")

val generatedSources = layout.projectDirectory.dir("src/language/generated")
val syntaxes = layout.projectDirectory.dir("syntaxes")

val script = layout.buildDirectory.file("langium/cli.mjs")
val extensionBundle = layout.buildDirectory.dir("vscode")

val generate = tasks.register<NpmTask>("langiumGenerate") {
    group = "build"
    dependsOn(tasks.npmInstall)
    npmCommand = listOf("run", "generate")
    inputs.file("langium-config.json")
    inputs.file(grammar)
    inputs.file(packageFile)
    inputs.file(lockFile)
    outputs.dir(generatedSources)
    outputs.dir(syntaxes)
}

val bundle = tasks.register<NpmTask>("bundle") {
    group = "build"
    dependsOn(tasks.npmInstall)
    npmCommand = listOf("run", "bundle")
    inputs.files(generate)
    inputs.dir(sources)
    inputs.file("esbuild.mjs")
    inputs.file(packageFile)
    inputs.file(lockFile)
    outputs.file(script)
    outputs.dir(extensionBundle)
}

val typecheck = tasks.register<NpmTask>("typecheck") {
    group = "verification"
    dependsOn(tasks.npmInstall)
    npmCommand = listOf("run", "typecheck")
    inputs.files(generate)
    inputs.dir(sources)
    inputs.files(tests)
    inputs.file("tsconfig.json")
    inputs.file(packageFile)
    inputs.file(lockFile)
}

val test = tasks.register<NpmTask>("test") {
    group = "verification"
    dependsOn(tasks.npmInstall)
    npmCommand = listOf("run", "test")
    inputs.files(generate)
    inputs.dir(sources)
    inputs.files(tests)
    inputs.file(packageFile)
    inputs.file(lockFile)
}

tasks.assemble {
    dependsOn(bundle)
}

tasks.check {
    dependsOn(typecheck, test)
}

tasks.clean {
    delete(generatedSources, syntaxes)
}

configurations.create("langiumCli") {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(CodegenPlugin.LANGUAGE_CLI_USAGE))
    outgoing.artifact(script) {
        builtBy(bundle)
    }
}

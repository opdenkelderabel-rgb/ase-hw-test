package hu.bme.mit.ftsrg.ase.gradle.regexgenerator

import hu.bme.mit.ftsrg.ase.gradle.codegen.CodegenExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

abstract class RegexGeneratorExtension {

    abstract val packageName: Property<String>
}

/**
 * Generates a class named after each `<name>.regex` file in `src/main/regex`.
 * Requires the regex language in `language` and the regex generator in `generator`.
 */
class RegexGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        pluginManager.apply("hu.bme.mit.ftsrg.ase.codegen")

        val regexGenerator = extensions.create<RegexGeneratorExtension>("regexGenerator").apply {
            packageName.convention("hu.bme.mit.ase.regex.generated")
        }

        extensions.configure<CodegenExtension> {
            models.from(layout.projectDirectory.dir("src/main/regex"))
            template = "regex.java.j2"
            outputPath = "{{ package | replace('.', '/') }}/{{ model.name | replace('-', ' ') | title | replace(' ', '') }}.java"
            variables.put("package", regexGenerator.packageName)
        }
    }
}

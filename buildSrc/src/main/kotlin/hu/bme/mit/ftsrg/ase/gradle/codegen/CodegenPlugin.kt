package hu.bme.mit.ftsrg.ase.gradle.codegen

import com.github.gradle.node.task.NodeTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

abstract class CodegenExtension {

    abstract val models: ConfigurableFileCollection

    /**
     * Path of the Jinja template within the templates of the generator.
     */
    abstract val template: Property<String>

    /**
     * Jinja template of the generated file path, rendered for each model.
     */
    abstract val outputPath: Property<String>

    abstract val variables: MapProperty<String, String>
}

/**
 * Generates Java sources from textual models with a Langium language and a Jinja generator.
 */
class CodegenPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        pluginManager.apply("java")
        pluginManager.apply("hu.bme.mit.ftsrg.ase.node")

        val codegen = extensions.create<CodegenExtension>("codegen")

        val language = configurations.dependencyScope(LANGUAGE_CONFIGURATION)
        val languageCli = configurations.resolvable(LANGUAGE_CLI_CONFIGURATION) {
            extendsFrom(language.get())
            attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(LANGUAGE_CLI_USAGE))
        }

        val generator = configurations.dependencyScope(GENERATOR_CONFIGURATION)
        val generatorTemplates = configurations.resolvable(GENERATOR_TEMPLATES_CONFIGURATION) {
            extendsFrom(generator.get())
            attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(TEMPLATES_USAGE))
        }

        val projectDirectory = layout.projectDirectory.asFile
        val cliScript = languageCli.flatMap { it.elements }.map { elements ->
            elements.singleOrNull()?.asFile ?: throw GradleException("The $LANGUAGE_CONFIGURATION configuration must hold exactly one language, but it resolved to ${elements.size}.")
        }
        val parsedModels = layout.buildDirectory.dir("generated/models")
        val parse = tasks.register<NodeTask>("parseModels") {
            group = "build"
            description = "Parses the models with the CLI of the language."
            script = layout.file(cliScript)
            args = parsedModels.zip(codegen.models.elements) { output, sources ->
                (listOf(output) + sources).map { it.asFile.relativeTo(projectDirectory).path }
            }
            inputs.files(codegen.models)
                .withPropertyName("models")
                .withPathSensitivity(PathSensitivity.RELATIVE)
                .ignoreEmptyDirectories()
                .skipWhenEmpty()
            outputs.dir(parsedModels).withPropertyName("parsedModels")
        }

        val generate = tasks.register<GenerateCode>("generateCode") {
            group = "build"
            description = "Generates Java sources from the parsed models."
            dependsOn(parse)
            models = parsedModels
            templates.from(generatorTemplates)
            template = codegen.template
            outputPath = codegen.outputPath
            variables = codegen.variables
            outputDirectory.convention(layout.projectDirectory.dir("src/generated/java"))
        }

        extensions.getByType<JavaPluginExtension>().sourceSets.named("main") {
            java.srcDir(generate.flatMap { it.outputDirectory })
        }
        tasks.named<Delete>("clean") {
            delete(layout.projectDirectory.dir("src/generated"))
        }
    }

    companion object {
        const val LANGUAGE_CONFIGURATION = "language"
        const val LANGUAGE_CLI_CONFIGURATION = "languageCli"
        const val LANGUAGE_CLI_USAGE = "langium-cli"
        const val GENERATOR_CONFIGURATION = "generator"
        const val GENERATOR_TEMPLATES_CONFIGURATION = "generatorTemplates"

        /**
         * Usage of the Jinja templates that a generator subproject publishes from `src/main/jinja`.
         */
        const val TEMPLATES_USAGE = "jinja-templates"
    }
}

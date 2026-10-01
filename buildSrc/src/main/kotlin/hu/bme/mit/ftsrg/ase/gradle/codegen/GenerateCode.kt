package hu.bme.mit.ftsrg.ase.gradle.codegen

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Renders [template] for each JSON model in [models].
 * Templates see the [variables] and `model`, which has a `path`, a `name` and a `root`.
 */
@CacheableTask
abstract class GenerateCode : DefaultTask() {

    @get:Internal
    abstract val models: DirectoryProperty

    /**
     * Directories of the Jinja templates.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val templates: ConfigurableFileCollection

    /**
     * Path of the rendered template within [templates].
     */
    @get:Input
    abstract val template: Property<String>

    /**
     * Jinja template of the generated file path, relative to [outputDirectory].
     */
    @get:Input
    abstract val outputPath: Property<String>

    @get:Input
    abstract val variables: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val modelFiles: FileTree
        get() = models.asFileTree.matching { include("**/*.json") }

    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun generate() {
        val renderer = TemplateRenderer(templates.files.filter { it.isDirectory })
        val mapper = ObjectMapper()
        val root = models.get().asFile
        val output = outputDirectory.get().asFile
        fileSystemOperations.delete {
            delete(output)
        }
        for (file in modelFiles.files.sorted()) {
            val model = mapOf(
                "path" to file.relativeTo(root).invariantSeparatorsPath.removeSuffix(".json"),
                "name" to file.name.substringBefore('.'),
                "root" to mapper.readValue(file, Any::class.java),
            )
            val context = variables.get() + ("model" to model)
            val target = output.resolve(renderer.renderText(outputPath.get(), context))
            if (target.exists()) {
                throw GradleException("More than one model is generated into $target")
            }
            target.parentFile.mkdirs()
            target.writeText(renderer.render(template.get(), context))
        }
    }
}

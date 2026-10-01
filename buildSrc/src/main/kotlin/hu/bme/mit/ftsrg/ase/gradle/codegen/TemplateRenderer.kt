package hu.bme.mit.ftsrg.ase.gradle.codegen

import com.hubspot.jinjava.Jinjava
import com.hubspot.jinjava.JinjavaConfig
import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import org.gradle.api.GradleException
import java.io.File
import java.io.IOException

/**
 * Renders Jinja templates from the given root directories.
 * Any template error fails the build.
 */
internal class TemplateRenderer(private val roots: List<File>) {

    private val jinjava = Jinjava(
        JinjavaConfig.newBuilder()
            .withEnableRecursiveMacroCalls(true)
            .withMaxMacroRecursionDepth(MAX_RECURSION_DEPTH)
            .withFailOnUnknownTokens(true)
            .build()
    ).apply {
        setResourceLocator { name, _, _ ->
            read(name)
        }
        globalContext.registerFilter(JavaStringFilter)
        globalContext.registerFilter(JavaCharFilter)
    }

    fun render(template: String, context: Map<String, Any?>): String {
        return render(template, read(template), context)
    }

    fun renderText(source: String, context: Map<String, Any?>): String {
        return render(source, source, context)
    }

    private fun render(name: String, source: String, context: Map<String, Any?>): String {
        val result = jinjava.renderForResult(source, context)
        if (result.errors.isNotEmpty()) {
            val errors = result.errors.joinToString("\n") { error ->
                val causes = generateSequence(error.exception as Throwable?) { it.cause }
                val cause = causes.lastOrNull()
                "  line ${error.lineno}: ${error.message}" + (cause?.let { " ($it)" } ?: "")
            }
            throw GradleException("Template $name failed to render:\n$errors")
        }
        return result.output
    }

    private fun read(name: String): String {
        val file = roots.map { it.resolve(name) }.firstOrNull { it.isFile }
            ?: throw IOException("Template not found: $name")
        return file.readText()
    }

    private companion object {
        const val MAX_RECURSION_DEPTH = 1000
    }
}

/**
 * Formats a value as a Java string literal: `{{ value | java_string }}`.
 */
internal object JavaStringFilter : Filter {

    override fun getName(): String {
        return "java_string"
    }

    override fun filter(value: Any?, interpreter: JinjavaInterpreter, vararg args: String) = buildString {
        append('"')
        (value?.toString() ?: "").codePoints().forEach {
            appendEscaped(it, '"')
        }
        append('"')
    }
}

/**
 * Formats a character or a UTF-16 code unit as a Java char literal: `{{ value | java_char }}`.
 */
internal object JavaCharFilter : Filter {

    override fun getName() = "java_char"

    override fun filter(value: Any?, interpreter: JinjavaInterpreter, vararg args: String): Any {
        val codeUnit = when {
            value is Number -> value.toInt()
            value is String && value.length == 1 -> value[0].code
            else -> throw GradleException("java_char expects a single character, got: $value")
        }
        if (codeUnit !in 0..0xFFFF) {
            throw GradleException("java_char expects a UTF-16 code unit, got: $codeUnit")
        }
        return buildString {
            append('\'')
            appendEscaped(codeUnit, '\'')
            append('\'')
        }
    }
}

private fun StringBuilder.appendEscaped(codePoint: Int, quote: Char) {
    when (codePoint) {
        '\\'.code -> append("\\\\")
        '\n'.code -> append("\\n")
        '\r'.code -> append("\\r")
        '\t'.code -> append("\\t")
        '\b'.code -> append("\\b")
        '\u000C'.code -> append("\\f")
        quote.code -> append('\\').append(quote)
        in 0x20..0x7E -> append(codePoint.toChar())
        else -> Character.toChars(codePoint).forEach { append("\\u%04x".format(it.code)) }
    }
}

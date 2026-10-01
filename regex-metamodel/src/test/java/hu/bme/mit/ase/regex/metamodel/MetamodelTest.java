package hu.bme.mit.ase.regex.metamodel;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import tools.refinery.generator.GeneratorResult;
import tools.refinery.generator.ModelSemantics;
import tools.refinery.generator.ProblemLoader;
import tools.refinery.generator.standalone.StandaloneRefinery;
import tools.refinery.language.model.problem.Problem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Checks the metamodel in {@code src/main/refinery} and the example models in {@code src/test/refinery}.
 */
class MetamodelTest {

    private static final Path MODELS = Path.of("src/test/refinery");

    @Test
    void loadsWithoutErrors() {
        assertThatCode(MetamodelTest::loadMetamodel).doesNotThrowAnyException();
    }

    @Test
    void isConsistent() throws IOException {
        try (var semantics = createSemantics(loadMetamodel())) {
            var result = semantics.checkConsistency();
            assertThat(result.isConsistent()).as(result::formatMessage).isTrue();
        }
    }

    @Test
    void isNotUnsatisfiable() throws IOException {
        try (var generator = StandaloneRefinery.getGeneratorFactory().createGenerator(loadMetamodel())) {
            generator.setRandomSeed(1);
            var result = generator.tryGenerateWithTimeout(60, TimeUnit.SECONDS);
            assertThat(result).isNotEqualTo(GeneratorResult.UNSATISFIABLE);
        }
    }

    @TestFactory
    List<DynamicTest> acceptsConsistentModels() throws IOException {
        return models(MODELS.resolve("consistent")).stream()
                .map(model -> dynamicTest(model.toString(), () -> {
                    try (var semantics = createSemantics(problemLoader().loadFile(model.toFile()))) {
                        var result = semantics.checkConsistency();
                        assertThat(result.isConsistent()).as(result::formatMessage).isTrue();
                    }
                }))
                .toList();
    }

    @TestFactory
    List<DynamicTest> rejectsInconsistentModels() throws IOException {
        return models(MODELS.resolve("inconsistent")).stream()
                .map(model -> dynamicTest(model.toString(), () -> {
                    var error = "regex::" + model.getParent().getFileName();
                    try (var semantics = createSemantics(problemLoader().loadFile(model.toFile()))) {
                        var result = semantics.checkConsistency();
                        assertThat(result.inconsistencies())
                                .as(result::formatMessage)
                                .extracting(inconsistency -> inconsistency.partialSymbol().name())
                                .contains(error);
                    }
                }))
                .toList();
    }

    private static List<Path> models(Path directory) throws IOException {
        try (var files = Files.walk(directory)) {
            return files.filter(file -> file.toString().endsWith(".problem")).sorted().toList();
        }
    }

    private static Problem loadMetamodel() throws IOException {
        return problemLoader().loadString("import regex.");
    }

    private static ModelSemantics createSemantics(Problem problem) {
        return StandaloneRefinery.getSemanticsFactory().createSemantics(problem);
    }

    private static ProblemLoader problemLoader() {
        return StandaloneRefinery.getProblemLoader().extraPath(Path.of("src/main/refinery"));
    }
}

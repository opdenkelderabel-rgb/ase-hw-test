import { existsSync } from 'node:fs';
import * as fs from 'node:fs/promises';
import * as path from 'node:path';
import { URI } from 'langium';
import { NodeFileSystem } from 'langium/node';
import { createRegexServices } from '../language/regex-module.js';

const [outputDirectory, ...sourceDirectories] = process.argv.slice(2);
const services = createRegexServices(NodeFileSystem).Regex;
const { LangiumDocuments, DocumentBuilder } = services.shared.workspace;

const files: { file: string; source: string }[] = [];
for (const directory of sourceDirectories.filter((directory) => existsSync(directory))) {
    for (const file of await fs.readdir(directory, { recursive: true })) {
        if (services.LanguageMetaData.fileExtensions.includes(path.extname(file))) {
            files.push({ file, source: path.resolve(directory, file) });
        }
    }
}
files.sort((left, right) => left.file.localeCompare(right.file));

const documents = await Promise.all(
    files.map(({ source }) => LangiumDocuments.getOrCreateDocument(URI.file(source))),
);
await DocumentBuilder.build(documents, { validation: true });

let failed = false;
for (const [index, document] of documents.entries()) {
    const errors = (document.diagnostics ?? []).filter((diagnostic) => diagnostic.severity === 1);
    for (const error of errors) {
        const { line, character } = error.range.start;
        console.error(`${files[index].source}:${line + 1}:${character + 1}: ${error.message}`);
    }
    failed ||= errors.length > 0;
}
if (failed) {
    process.exit(1);
}

await fs.rm(outputDirectory, { recursive: true, force: true });
for (const [index, document] of documents.entries()) {
    const target = path.join(outputDirectory, `${files[index].file}.json`);
    await fs.mkdir(path.dirname(target), { recursive: true });
    const json = services.serializer.JsonSerializer.serialize(document.parseResult.value, {
        space: 2,
        refText: true,
    });
    await fs.writeFile(target, json);
}

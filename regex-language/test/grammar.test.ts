import { readdirSync, readFileSync } from 'node:fs';
import * as path from 'node:path';
import { EmptyFileSystem, URI } from 'langium';
import { expect, test } from 'vitest';
import type { Diagnostic } from 'vscode-languageserver';
import { createRegexServices } from '../src/language/regex-module.js';

// Every regex in `test/valid` must be accepted, and every regex in `test/invalid` must be rejected.

const services = createRegexServices(EmptyFileSystem).Regex;

async function errors(file: string): Promise<Diagnostic[]> {
    const document = services.shared.workspace.LangiumDocumentFactory.fromString(
        readFileSync(file, 'utf-8'),
        URI.file(path.resolve(file)),
    );
    await services.shared.workspace.DocumentBuilder.build([document], { validation: true });
    return (document.diagnostics ?? []).filter((diagnostic) => diagnostic.severity === 1);
}

function regexFiles(directory: string): string[] {
    return readdirSync(directory)
        .filter((file) => file.endsWith('.regex'))
        .sort()
        .map((file) => path.join(directory, file));
}

test.each(regexFiles('test/valid'))('accepts %s', async (file) => {
    expect(await errors(file)).toEqual([]);
});

test.each(regexFiles('test/invalid'))('rejects %s', async (file) => {
    expect(await errors(file)).not.toEqual([]);
});

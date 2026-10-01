import * as esbuild from 'esbuild';

const cli = esbuild.build({
    entryPoints: ['src/cli/main.ts'],
    outfile: 'build/langium/cli.mjs',
    bundle: true,
    platform: 'node',
    format: 'esm',
    banner: {
        // Bundled CommonJS dependencies call `require`, which ES modules do not define.
        js: "import { createRequire } from 'node:module'; const require = createRequire(import.meta.url);",
    },
});

const extension = esbuild.build({
    entryPoints: ['src/extension/main.ts', 'src/language/main.ts'],
    outbase: 'src',
    outdir: 'build/vscode',
    outExtension: { '.js': '.cjs' },
    bundle: true,
    platform: 'node',
    format: 'cjs',
    external: ['vscode'],
});

await Promise.all([cli, extension]);

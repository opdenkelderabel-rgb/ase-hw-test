import * as path from 'node:path';
import type * as vscode from 'vscode';
import {
    LanguageClient,
    TransportKind,
    type LanguageClientOptions,
    type ServerOptions,
} from 'vscode-languageclient/node';

let client: LanguageClient | undefined;

export async function activate(context: vscode.ExtensionContext): Promise<void> {
    const serverModule = context.asAbsolutePath(
        path.join('build', 'vscode', 'language', 'main.cjs'),
    );
    const serverOptions: ServerOptions = {
        run: { module: serverModule, transport: TransportKind.ipc },
        debug: { module: serverModule, transport: TransportKind.ipc },
    };
    const clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'regex' }],
    };
    client = new LanguageClient('regex', 'Regex', serverOptions, clientOptions);
    await client.start();
}

export async function deactivate(): Promise<void> {
    await client?.stop();
}

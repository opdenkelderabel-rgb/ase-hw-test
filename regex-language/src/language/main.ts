import { startLanguageServer } from 'langium/lsp';
import { NodeFileSystem } from 'langium/node';
import { createConnection, ProposedFeatures } from 'vscode-languageserver/node';
import { createRegexServices } from './regex-module.js';

const connection = createConnection(ProposedFeatures.all);
const { shared } = createRegexServices({ connection, ...NodeFileSystem });
startLanguageServer(shared);

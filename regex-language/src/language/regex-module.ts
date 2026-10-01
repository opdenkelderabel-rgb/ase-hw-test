import { inject, type Module } from 'langium';
import {
    createDefaultModule,
    createDefaultSharedModule,
    type DefaultSharedModuleContext,
    type LangiumServices,
    type LangiumSharedServices,
    type PartialLangiumServices,
} from 'langium/lsp';
import { RegexDslGeneratedModule, RegexGeneratedSharedModule } from './generated/module.js';
import { RegexScopeComputation } from './regex-scope-computation.js';
import { RegexTokenBuilder } from './regex-token-builder.js';
import { RegexValueConverter } from './regex-value-converter.js';

export const RegexModule: Module<LangiumServices, PartialLangiumServices> = {
    parser: {
        TokenBuilder: () => new RegexTokenBuilder(),
        ValueConverter: () => new RegexValueConverter(),
    },
    references: {
        ScopeComputation: (services) => new RegexScopeComputation(services),
    },
};

export function createRegexServices(context: DefaultSharedModuleContext): {
    shared: LangiumSharedServices;
    Regex: LangiumServices;
} {
    const shared = inject(createDefaultSharedModule(context), RegexGeneratedSharedModule);
    const Regex = inject(createDefaultModule({ shared }), RegexDslGeneratedModule, RegexModule);
    shared.ServiceRegistry.register(Regex);
    if (!context.connection) {
        // Without a language server, no client sends the configuration.
        shared.workspace.ConfigurationProvider.initialized({});
    }
    return { shared, Regex };
}

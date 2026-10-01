import { DefaultScopeComputation, type AstNodeDescription } from 'langium';

/**
 * Exports no definitions, so references resolve only within their own file.
 */
export class RegexScopeComputation extends DefaultScopeComputation {
    override async collectExportedSymbols(): Promise<AstNodeDescription[]> {
        return [];
    }
}

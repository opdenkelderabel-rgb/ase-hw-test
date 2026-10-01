import {
    DefaultTokenBuilder,
    GrammarAST,
    isTokenTypeArray,
    type TokenBuilderOptions,
} from 'langium';

const DEFAULT_MODE = 'default';
const CHARACTER_CLASS_MODE = 'characterClass';
const CHARACTER_CLASS_TOKENS = ['WS', '^', '-', ']', 'CHARACTER', 'ESCAPED_CHARACTER'];

/**
 * Lexes the inside of character classes in a separate mode.
 * There, every character stands for itself instead of being part of a name or a keyword.
 */
export class RegexTokenBuilder extends DefaultTokenBuilder {

    override buildTokens(grammar: GrammarAST.Grammar, options?: TokenBuilderOptions) {
        const tokens = super.buildTokens(grammar, options);
        if (!isTokenTypeArray(tokens)) {
            throw new Error('Expected the tokens of a single lexer mode.');
        }
        const defaultTokens = tokens.filter(
            (token) => token.name === 'WS' || !CHARACTER_CLASS_TOKENS.includes(token.name),
        );
        const characterClassTokens = tokens.filter(
            (token) => CHARACTER_CLASS_TOKENS.includes(token.name),
        );
        for (const token of defaultTokens) {
            // A token may only have longer alternatives in its own mode.
            if (Array.isArray(token.LONGER_ALT)) {
                token.LONGER_ALT = token.LONGER_ALT.filter(
                    (alternative) => defaultTokens.includes(alternative),
                );
            }
            if (token.name === '[') {
                token.PUSH_MODE = CHARACTER_CLASS_MODE;
            }
        }
        for (const token of characterClassTokens) {
            if (token.name === ']') {
                token.POP_MODE = true;
            }
        }
        return {
            modes: {
                [DEFAULT_MODE]: defaultTokens,
                [CHARACTER_CLASS_MODE]: characterClassTokens,
            },
            defaultMode: DEFAULT_MODE,
        };
    }
}

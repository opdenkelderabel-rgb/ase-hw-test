import { DefaultValueConverter, GrammarAST, type CstNode, type ValueType } from 'langium';

export class RegexValueConverter extends DefaultValueConverter {
    protected override runConverter(
        rule: GrammarAST.AbstractRule,
        input: string,
        cstNode: CstNode,
    ): ValueType {
        switch (rule.name) {
            case 'STRING_LITERAL':
                return unescape(input.slice(1, -1));
            case 'ESCAPED_CHARACTER':
                return unescape(input);
            default:
                return super.runConverter(rule, input, cstNode);
        }
    }
}

function unescape(text: string): string {
    return text.replace(/\\(.)/g, '$1');
}

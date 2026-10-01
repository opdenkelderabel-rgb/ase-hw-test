grammar RegexDsl

// TODO: write here your grammar specification.

// These are Langium regular expressions, not the regexes of our language.
terminal ID: /[a-zA-Z0-9_]+/;
terminal STRING_LITERAL: /'([^'\\]|\\.)+'/;

// Only lexed between `[` and `]`, for the elements of character groups.
terminal CHARACTER: /[^\s\-\[\]\^\/\\]/;
terminal ESCAPED_CHARACTER: /\\[^\s]/;

hidden terminal WS: /\s+/;
hidden terminal SL_COMMENT: /\/\/[^\n\r]*/;
hidden terminal ML_COMMENT: /\/\*[\s\S]*?\*\//;

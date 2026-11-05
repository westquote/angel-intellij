package com.sarahshandcraftedentertainment.angelintellij.editor;

import com.sarahshandcraftedentertainment.angelintellij.language.AngelScriptLexerAdapter;
import com.sarahshandcraftedentertainment.angelintellij.psi.AngelScriptTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Lexer-driven colouring for .as files. The LSP semantic tokens (see AngelScriptSemanticTokensColorProvider)
 * layer richer colours on top when the server supplies them; this is the baseline that is always there.
 */
public class AngelScriptSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("ANGELSCRIPT_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    // The lexer emits one COMMENT token for both // and /* */ forms, so both share the line-comment colour.
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("ANGELSCRIPT_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("ANGELSCRIPT_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("ANGELSCRIPT_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("ANGELSCRIPT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("ANGELSCRIPT_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("ANGELSCRIPT_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("ANGELSCRIPT_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("ANGELSCRIPT_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey SEMICOLON =
            createTextAttributesKey("ANGELSCRIPT_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("ANGELSCRIPT_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey DOT =
            createTextAttributesKey("ANGELSCRIPT_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("ANGELSCRIPT_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    // Rider's C++ lexical keys, layered over the AngelScript ones: where the scheme styles them, .as text takes the C++
    // look, otherwise the AngelScript key shows through. Looked up only, never registered with a fallback of ours.
    private static final TextAttributesKey CPP_KEYWORD = TextAttributesKey.find("ReSharper.CPP_KEYWORD");
    private static final TextAttributesKey CPP_LINE_COMMENT = TextAttributesKey.find("ReSharper.CPP_LINE_COMMENT");
    private static final TextAttributesKey CPP_STRING = TextAttributesKey.find("ReSharper.CPP_STRING_LITERAL");
    private static final TextAttributesKey CPP_OPERATOR = TextAttributesKey.find("ReSharper.CPP_OPERATOR_SIGN");

    private static final TokenSet OPERATORS = TokenSet.create(
            AngelScriptTokenTypes.PLUS, AngelScriptTokenTypes.MINUS, AngelScriptTokenTypes.STAR,
            AngelScriptTokenTypes.SLASH, AngelScriptTokenTypes.PERCENT, AngelScriptTokenTypes.STAR_STAR,
            AngelScriptTokenTypes.AMP, AngelScriptTokenTypes.BIT_OR, AngelScriptTokenTypes.BIT_XOR,
            AngelScriptTokenTypes.BIT_NOT, AngelScriptTokenTypes.NOT, AngelScriptTokenTypes.AND,
            AngelScriptTokenTypes.OR, AngelScriptTokenTypes.XOR, AngelScriptTokenTypes.EQUAL,
            AngelScriptTokenTypes.NOT_EQUAL, AngelScriptTokenTypes.NOT_IS, AngelScriptTokenTypes.LESS_THAN,
            AngelScriptTokenTypes.GREATER_THAN, AngelScriptTokenTypes.LESS_THAN_OR_EQUAL,
            AngelScriptTokenTypes.GREATER_THAN_OR_EQUAL, AngelScriptTokenTypes.ASSIGNMENT,
            AngelScriptTokenTypes.ADD_ASSIGN, AngelScriptTokenTypes.SUB_ASSIGN, AngelScriptTokenTypes.MUL_ASSIGN,
            AngelScriptTokenTypes.DIV_ASSIGN, AngelScriptTokenTypes.MOD_ASSIGN, AngelScriptTokenTypes.POW_ASSIGN,
            AngelScriptTokenTypes.AND_ASSIGN, AngelScriptTokenTypes.OR_ASSIGN, AngelScriptTokenTypes.XOR_ASSIGN,
            AngelScriptTokenTypes.SHIFT_LEFT_ASSIGN, AngelScriptTokenTypes.SHIFT_RIGHT_A_ASSIGN,
            AngelScriptTokenTypes.SHIFT_RIGHT_L_ASSIGN, AngelScriptTokenTypes.BIT_SHIFT_LEFT,
            AngelScriptTokenTypes.BIT_SHIFT_RIGHT, AngelScriptTokenTypes.BIT_SHIFT_RIGHT_ARITH,
            AngelScriptTokenTypes.INC, AngelScriptTokenTypes.DEC, AngelScriptTokenTypes.QUESTION,
            AngelScriptTokenTypes.COLON, AngelScriptTokenTypes.SCOPE, AngelScriptTokenTypes.VARIADIC);

    private static final TokenSet PARENTHESES_TOKENS = TokenSet.create(
            AngelScriptTokenTypes.OPEN_PARENTHESIS, AngelScriptTokenTypes.CLOSE_PARENTHESIS);
    private static final TokenSet BRACKET_TOKENS = TokenSet.create(
            AngelScriptTokenTypes.OPEN_BRACKET, AngelScriptTokenTypes.CLOSE_BRACKET);
    private static final TokenSet BRACE_TOKENS = TokenSet.create(
            AngelScriptTokenTypes.START_STATEMENT_BLOCK, AngelScriptTokenTypes.END_STATEMENT_BLOCK);

    private static final TextAttributesKey[] KEYWORD_KEYS = pack(KEYWORD, CPP_KEYWORD);
    private static final TextAttributesKey[] COMMENT_KEYS = pack(COMMENT, CPP_LINE_COMMENT);
    private static final TextAttributesKey[] STRING_KEYS = pack(STRING, CPP_STRING);
    private static final TextAttributesKey[] NUMBER_KEYS = pack(NUMBER);
    private static final TextAttributesKey[] IDENTIFIER_KEYS = pack(IDENTIFIER);
    private static final TextAttributesKey[] OPERATOR_KEYS = pack(OPERATOR, CPP_OPERATOR);
    private static final TextAttributesKey[] PARENTHESES_KEYS = pack(PARENTHESES);
    private static final TextAttributesKey[] BRACKETS_KEYS = pack(BRACKETS);
    private static final TextAttributesKey[] BRACES_KEYS = pack(BRACES);
    private static final TextAttributesKey[] SEMICOLON_KEYS = pack(SEMICOLON);
    private static final TextAttributesKey[] COMMA_KEYS = pack(COMMA);
    private static final TextAttributesKey[] DOT_KEYS = pack(DOT);
    private static final TextAttributesKey[] BAD_CHARACTER_KEYS = pack(BAD_CHARACTER);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new AngelScriptLexerAdapter();
    }

    @Override
    public @NotNull TextAttributesKey[] getTokenHighlights(IElementType type) {
        if (type == AngelScriptTokenTypes.KEYWORD) return KEYWORD_KEYS;
        if (type == AngelScriptTokenTypes.COMMENT) return COMMENT_KEYS;
        if (type == AngelScriptTokenTypes.STRING) return STRING_KEYS;
        if (type == AngelScriptTokenTypes.NUMBER) return NUMBER_KEYS;
        if (type == AngelScriptTokenTypes.IDENTIFIER) return IDENTIFIER_KEYS;
        if (type == AngelScriptTokenTypes.DOT) return DOT_KEYS;
        if (type == AngelScriptTokenTypes.END_STATEMENT) return SEMICOLON_KEYS;
        if (type == AngelScriptTokenTypes.LIST_SEPARATOR) return COMMA_KEYS;
        if (PARENTHESES_TOKENS.contains(type)) return PARENTHESES_KEYS;
        if (BRACKET_TOKENS.contains(type)) return BRACKETS_KEYS;
        if (BRACE_TOKENS.contains(type)) return BRACES_KEYS;
        if (OPERATORS.contains(type)) return OPERATOR_KEYS;
        if (type == AngelScriptTokenTypes.UNKNOWN) return BAD_CHARACTER_KEYS;
        return TextAttributesKey.EMPTY_ARRAY;
    }
}

package com.sarahshandcraftedentertainment.angelintellij.lsp;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.lsp4ij.features.semanticTokens.SemanticTokensColorsProvider;
import com.redhat.devtools.lsp4ij.features.semanticTokens.SemanticTokensHighlightingColors;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Reference: https://github.com/redhat-developer/lsp4ij/blob/main/src/main/java/com/redhat/devtools/lsp4ij/features/semanticTokens/DefaultSemanticTokensColorsProvider.java

/**
 * Colours AngelScript identifiers with Rider's own C++ keys (ReSharper.CPP_*), so .as files follow whatever the
 * colour scheme gives C++. Outside Rider, or when a scheme leaves a C++ key empty, the LSP4IJ key is used instead.
 */
public class AngelScriptSemanticTokensColorProvider implements SemanticTokensColorsProvider {

    // Rider's C++ identifier keys; names from the ReSharper C++ backend and Rider's bundled colour schemes.
    static final String CPP_NAMESPACE = "ReSharper.CPP_NAMESPACE_IDENTIFIER";
    static final String CPP_CLASS = "ReSharper.CPP_CLASS_IDENTIFIER";
    static final String CPP_STRUCT = "ReSharper.CPP_STRUCT_IDENTIFIER";
    static final String CPP_TYPEDEF = "ReSharper.CPP_TYPEDEF_IDENTIFIER";
    static final String CPP_TEMPLATE_PARAMETER = "ReSharper.CPP_TEMPLATE_PARAMETER_IDENTIFIER";
    static final String CPP_ENUM = "ReSharper.CPP_ENUM_IDENTIFIER";
    static final String CPP_ENUMERATOR = "ReSharper.CPP_ENUM_ENUMERATOR_IDENTIFIER";
    static final String CPP_GLOBAL_FUNCTION = "ReSharper.CPP_GLOBAL_FUNCTION_IDENTIFIER";
    static final String CPP_MEMBER_FUNCTION = "ReSharper.CPP_MEMBER_FUNCTION_IDENTIFIER";
    static final String CPP_STATIC_MEMBER_FUNCTION = "ReSharper.CPP_STATIC_MEMBER_FUNCTION_IDENTIFIER";
    static final String CPP_CLASS_FIELD = "ReSharper.CPP_CLASS_FIELD_IDENTIFIER";
    static final String CPP_STATIC_FIELD = "ReSharper.CPP_STATIC_FIELD_IDENTIFIER";
    static final String CPP_CONSTANT = "ReSharper.CPP_CONSTANT_IDENTIFIER";
    static final String CPP_LOCAL_VARIABLE = "ReSharper.CPP_LOCAL_VARIABLE_IDENTIFIER";
    static final String CPP_GLOBAL_VARIABLE = "ReSharper.CPP_GLOBAL_VARIABLE_IDENTIFIER";
    static final String CPP_PARAMETER = "ReSharper.CPP_PARAMETER_VARIABLE_IDENTIFIER";
    static final String CPP_MACRO = "ReSharper.CPP_PREPROCESSOR_MACRO_IDENTIFIER";
    static final String CPP_CONTROL_FLOW_KEYWORD = "ReSharper.CPP_CONTROL_FLOW_KEYWORD";

    @Override
    public @Nullable TextAttributesKey getTextAttributesKey(@NotNull String tokenType,
                                                            @NotNull List<String> tokenModifiers,
                                                            @NotNull PsiFile file) {
        boolean isStatic = tokenModifiers.contains(SemanticTokenModifiers.Static);
        boolean isReadonly = tokenModifiers.contains(SemanticTokenModifiers.Readonly);
        boolean isDeclaration = tokenModifiers.contains(SemanticTokenModifiers.Declaration)
                || tokenModifiers.contains(SemanticTokenModifiers.Definition);

        switch (tokenType) {
            case SemanticTokenTypes.Namespace:
                return cpp(CPP_NAMESPACE, isDeclaration
                        ? SemanticTokensHighlightingColors.NAMESPACE_DECLARATION
                        : SemanticTokensHighlightingColors.NAMESPACE);

            case SemanticTokenTypes.Class:
                return cpp(CPP_CLASS, isDeclaration
                        ? SemanticTokensHighlightingColors.CLASS_DECLARATION
                        : SemanticTokensHighlightingColors.CLASS);

            case SemanticTokenTypes.Interface:
                return cpp(CPP_CLASS, SemanticTokensHighlightingColors.INTERFACE);

            case SemanticTokenTypes.Struct:
                return cpp(CPP_STRUCT, SemanticTokensHighlightingColors.STRUCT);

            case SemanticTokenTypes.Type:
                return cpp(CPP_TYPEDEF, SemanticTokensHighlightingColors.TYPE);

            case SemanticTokenTypes.TypeParameter:
                return cpp(CPP_TEMPLATE_PARAMETER, SemanticTokensHighlightingColors.TYPE_PARAMETER);

            case SemanticTokenTypes.Enum:
                return cpp(CPP_ENUM, SemanticTokensHighlightingColors.ENUM);

            case SemanticTokenTypes.EnumMember:
                return cpp(CPP_ENUMERATOR, SemanticTokensHighlightingColors.ENUM_MEMBER);

            case SemanticTokenTypes.Function:
                return cpp(CPP_GLOBAL_FUNCTION, isDeclaration
                        ? SemanticTokensHighlightingColors.FUNCTION_DECLARATION
                        : SemanticTokensHighlightingColors.FUNCTION);

            case SemanticTokenTypes.Method:
                if (isStatic) {
                    return cpp(CPP_STATIC_MEMBER_FUNCTION, SemanticTokensHighlightingColors.STATIC_METHOD);
                }
                return cpp(CPP_MEMBER_FUNCTION, isDeclaration
                        ? SemanticTokensHighlightingColors.METHOD_DECLARATION
                        : SemanticTokensHighlightingColors.METHOD);

            case SemanticTokenTypes.Property:
                if (isStatic && isReadonly) {
                    return cpp(CPP_CONSTANT, SemanticTokensHighlightingColors.STATIC_READONLY_PROPERTY);
                }
                if (isStatic) {
                    return cpp(CPP_STATIC_FIELD, SemanticTokensHighlightingColors.STATIC_PROPERTY);
                }
                return cpp(CPP_CLASS_FIELD, SemanticTokensHighlightingColors.PROPERTY);

            case SemanticTokenTypes.Variable:
                if (isStatic && isReadonly) {
                    return cpp(CPP_CONSTANT, SemanticTokensHighlightingColors.STATIC_READONLY_VARIABLE);
                }
                if (isStatic) {
                    return cpp(CPP_GLOBAL_VARIABLE, SemanticTokensHighlightingColors.STATIC_VARIABLE);
                }
                // LSP4IJ's VARIABLE falls back to the reassigned-local style (underlined); a C++ local is plain.
                return cpp(CPP_LOCAL_VARIABLE, SemanticTokensHighlightingColors.READONLY_VARIABLE);

            case SemanticTokenTypes.Parameter:
                return cpp(CPP_PARAMETER, SemanticTokensHighlightingColors.PARAMETER);

            case SemanticTokenTypes.Macro:
            case "directive": // unique to angel-lsp
                return cpp(CPP_MACRO, SemanticTokensHighlightingColors.MACRO);

            case SemanticTokenTypes.Decorator:
                return SemanticTokensHighlightingColors.DECORATOR;

            case SemanticTokenTypes.Event:
                return SemanticTokensHighlightingColors.EVENT;

            case "label":
                // LSP doesn't define "label", but vscode defines it
                return SemanticTokensHighlightingColors.LABEL;

            // Lexical tokens get no semantic colour: the syntax highlighter already colours them with the C++ keys.
            case SemanticTokenTypes.Comment:
            case SemanticTokenTypes.String:
            case SemanticTokenTypes.Keyword:
            case SemanticTokenTypes.Modifier:
            case SemanticTokenTypes.Number:
            case SemanticTokenTypes.Operator:
            case "builtin": // unique to angel-lsp
                return null;

            case "controlKeyword": // unique to angel-lsp: if, for, return...
                return cppOrNull(CPP_CONTROL_FLOW_KEYWORD);

            case SemanticTokenTypes.Regexp:
                return SemanticTokensHighlightingColors.REGEXP;
        }
        return null;
    }

    /**
     * Rider's key when the current scheme gives it any style (its own or through Rider's fallback chain), else the
     * given fallback. The Rider key is looked up, never registered with a fallback of ours, so Rider's registration
     * stays authoritative.
     */
    static @NotNull TextAttributesKey cpp(@NotNull String riderKeyName, @NotNull TextAttributesKey fallback) {
        TextAttributesKey riderKey = cppOrNull(riderKeyName);
        return riderKey != null ? riderKey : fallback;
    }

    static @Nullable TextAttributesKey cppOrNull(@NotNull String riderKeyName) {
        TextAttributesKey riderKey = TextAttributesKey.find(riderKeyName);
        TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(riderKey);
        return attributes != null && !attributes.isEmpty() ? riderKey : null;
    }
}

package com.sarahshandcraftedentertainment.angelintellij.editor;

import com.sarahshandcraftedentertainment.angelintellij.language.AngelScriptIcon;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/** Settings > Editor > Color Scheme > AngelScript: lets the lexer colours be edited and previewed. */
public class AngelScriptColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", AngelScriptSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Comment", AngelScriptSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("String", AngelScriptSyntaxHighlighter.STRING),
            new AttributesDescriptor("Number", AngelScriptSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Identifier", AngelScriptSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Operator", AngelScriptSyntaxHighlighter.OPERATOR),
            new AttributesDescriptor("Braces and Operators//Parentheses", AngelScriptSyntaxHighlighter.PARENTHESES),
            new AttributesDescriptor("Braces and Operators//Brackets", AngelScriptSyntaxHighlighter.BRACKETS),
            new AttributesDescriptor("Braces and Operators//Braces", AngelScriptSyntaxHighlighter.BRACES),
            new AttributesDescriptor("Braces and Operators//Semicolon", AngelScriptSyntaxHighlighter.SEMICOLON),
            new AttributesDescriptor("Braces and Operators//Comma", AngelScriptSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Braces and Operators//Dot", AngelScriptSyntaxHighlighter.DOT),
            new AttributesDescriptor("Bad character", AngelScriptSyntaxHighlighter.BAD_CHARACTER),
    };

    private static final String DEMO_TEXT =
            "// Math helpers shared by the scripted rig units.\n" +
            "/* Everything sits in a namespace so several\n" +
            "   libraries can be included without clashes. */\n" +
            "namespace RigMath\n" +
            "{\n" +
            "    const float kEpsilon = 0.0001f;\n" +
            "    const int kSteps = 0x10;\n" +
            "    const string kName = \"RigMath\";\n" +
            "\n" +
            "    float Clamp01(float v) { return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v); }\n" +
            "\n" +
            "    float Lerp(float a, float b, float t) { return a + (b - a) * t; }\n" +
            "\n" +
            "    float Remap01(float v, float inMin, float inMax)\n" +
            "    {\n" +
            "        return Clamp01((v - inMin) / Math::Max(inMax - inMin, kEpsilon));\n" +
            "    }\n" +
            "\n" +
            "    bool IsWithin(const FVector &in point, const FVector &in origin, float maxDist)\n" +
            "    {\n" +
            "        array<float> dists = {FVector::Distance(point, origin)};\n" +
            "        return dists[0] <= maxDist && !(maxDist < 0.0f);\n" +
            "    }\n" +
            "}\n";

    @Override
    public @Nullable Icon getIcon() {
        return AngelScriptIcon.AS;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new AngelScriptSyntaxHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return DEMO_TEXT;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "AngelScript";
    }
}

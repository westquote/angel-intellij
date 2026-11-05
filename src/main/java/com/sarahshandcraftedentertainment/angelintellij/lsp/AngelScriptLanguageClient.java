package com.sarahshandcraftedentertainment.angelintellij.lsp;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AngelScriptLanguageClient extends LanguageClientImpl {
    /** Settings section the server asks for in workspace/configuration. */
    static final String SECTION = "angelScript";

    /** String literal type of the Unreal AngelScript engine. */
    static final String ENGINE_STRING_TYPE = "FString";

    /** The engine only treats get_/set_ methods marked `property` as accessors. */
    static final boolean ENGINE_EXPLICIT_PROPERTY_ACCESSOR = true;

    public AngelScriptLanguageClient(Project project) {
        super(project);
    }

    // LSP4IJ answers workspace/configuration (and builds didChangeConfiguration) from the JSON stored in
    // LanguageServersSettings.xml, which is a frozen copy of whatever defaults the plugin shipped when it was
    // first installed. The language rules below are the engine's, not a preference, so they are applied on top
    // of the stored JSON every time; the user's other keys pass through unchanged.
    @Override
    protected @Nullable Object createSettings() {
        Object stored = super.createSettings();
        JsonObject settings = stored instanceof JsonObject json ? json.deepCopy() : new JsonObject();
        return applyEngineLanguageRules(settings);
    }

    /** Overrides the engine-owned language rules in a settings object, nested or dotted. */
    public static @NotNull JsonObject applyEngineLanguageRules(@NotNull JsonObject settings) {
        JsonElement section = settings.get(SECTION);
        boolean hasDottedKeys = settings.keySet().stream().anyMatch(key -> key.startsWith(SECTION + "."));
        if (section instanceof JsonObject || !hasDottedKeys) {
            // Expanded form (LSP4IJ's default), or nothing stored yet: write into the nested section.
            JsonObject target = section instanceof JsonObject nested ? nested : new JsonObject();
            target.addProperty("builtinStringType", ENGINE_STRING_TYPE);
            target.addProperty("explicitPropertyAccessor", ENGINE_EXPLICIT_PROPERTY_ACCESSOR);
            settings.add(SECTION, target);
        } else {
            // "Expand configuration" switched off: the keys stay dotted at the root.
            settings.addProperty(SECTION + ".builtinStringType", ENGINE_STRING_TYPE);
            settings.addProperty(SECTION + ".explicitPropertyAccessor", ENGINE_EXPLICIT_PROPERTY_ACCESSOR);
        }
        return settings;
    }
}

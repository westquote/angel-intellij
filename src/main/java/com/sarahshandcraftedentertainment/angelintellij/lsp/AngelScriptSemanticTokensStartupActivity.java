package com.sarahshandcraftedentertainment.angelintellij.lsp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Starts the semantic-token applier and colours the AngelScript editors restored with the project. */
public class AngelScriptSemanticTokensStartupActivity implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        AngelScriptSemanticTokensApplier.getInstance(project).scheduleOpenFiles();
        return null;
    }
}

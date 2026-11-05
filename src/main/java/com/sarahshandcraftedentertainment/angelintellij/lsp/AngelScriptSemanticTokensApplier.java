package com.sarahshandcraftedentertainment.angelintellij.lsp;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.Alarm;
import com.redhat.devtools.lsp4ij.LSPFileSupport;
import com.redhat.devtools.lsp4ij.LanguageServersRegistry;
import com.redhat.devtools.lsp4ij.features.semanticTokens.SemanticTokensData;
import com.sarahshandcraftedentertainment.angelintellij.language.AngelScriptFileType;
import com.sarahshandcraftedentertainment.angelintellij.language.AngelScriptPredefinedFileType;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies the language server's semantic tokens to open AngelScript documents as range highlighters.
 * <p>
 * LSP4IJ colours semantic tokens from a HighlightVisitor inside the daemon's general highlighting pass. Under Rider
 * that path left every AngelScript identifier uncoloured, while LSP4IJ's diagnostics, which it writes straight to the
 * document markup, do show. This writes the semantic tokens the same way: it asks LSP4IJ for the file's tokens (the
 * same cached request its visitor makes), maps them through {@link AngelScriptSemanticTokensColorProvider}, and
 * replaces this document's highlighters. Where the visitor does run, both paths paint the same keys.
 */
@Service(Service.Level.PROJECT)
public final class AngelScriptSemanticTokensApplier implements Disposable {

    private static final Logger LOG = Logger.getInstance(AngelScriptSemanticTokensApplier.class);

    private static final Key<List<RangeHighlighter>> HIGHLIGHTERS = Key.create("angelscript.semanticTokens.highlighters");
    private static final Key<Boolean> REPORTED = Key.create("angelscript.semanticTokens.reported");

    // Typing pause before re-requesting; also lets LSP4IJ send didChange and the PSI commit first.
    private static final int DEBOUNCE_MS = 300;

    private final Project project;
    private final Alarm alarm;
    private final Set<Document> pending = ConcurrentHashMap.newKeySet();

    public AngelScriptSemanticTokensApplier(@NotNull Project project) {
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                schedule(event.getDocument());
            }
        }, this);

        project.getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                new FileEditorManagerListener() {
                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        schedule(FileDocumentManager.getInstance().getDocument(file));
                    }
                });
    }

    public static @NotNull AngelScriptSemanticTokensApplier getInstance(@NotNull Project project) {
        return project.getService(AngelScriptSemanticTokensApplier.class);
    }

    /** Colours every AngelScript file already open, e.g. editors restored when the project opens. */
    public void scheduleOpenFiles() {
        for (VirtualFile file : FileEditorManager.getInstance(project).getOpenFiles()) {
            schedule(FileDocumentManager.getInstance().getDocument(file));
        }
    }

    private void schedule(@Nullable Document document) {
        if (document == null || project.isDisposed()) {
            return;
        }
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null || !isAngelScript(file)) {
            return;
        }
        pending.add(document);
        alarm.cancelAllRequests();
        alarm.addRequest(this::requestPending, DEBOUNCE_MS);
    }

    private static boolean isAngelScript(@NotNull VirtualFile file) {
        var type = file.getFileType();
        return type == AngelScriptFileType.INSTANCE || type == AngelScriptPredefinedFileType.INSTANCE;
    }

    private void requestPending() {
        for (Iterator<Document> it = pending.iterator(); it.hasNext(); ) {
            Document document = it.next();
            it.remove();
            request(document);
        }
    }

    private record Request(@NotNull PsiFile file, long stamp, @NotNull CompletableFuture<SemanticTokensData> tokens) {
    }

    private void request(@NotNull Document document) {
        if (project.isDisposed()) {
            return;
        }
        Request request = ReadAction.compute(() -> {
            PsiDocumentManager psiDocuments = PsiDocumentManager.getInstance(project);
            if (!psiDocuments.isCommitted(document)) {
                return null;
            }
            PsiFile file = psiDocuments.getPsiFile(document);
            if (file == null || !LanguageServersRegistry.getInstance().isFileSupported(file)) {
                return null;
            }
            var params = new SemanticTokensParams(new TextDocumentIdentifier());
            var tokens = LSPFileSupport.getSupport(file).getSemanticTokensSupport().getSemanticTokens(params);
            return new Request(file, document.getModificationStamp(), tokens);
        });
        if (request == null) {
            // Not committed yet (or not served): retry after the commit.
            if (!PsiDocumentManager.getInstance(project).isCommitted(document)) {
                schedule(document);
            }
            return;
        }
        request.tokens().whenComplete((data, error) -> {
            if (error != null) {
                Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
                if (!(cause instanceof CancellationException)) {
                    LOG.warn("AngelScript semantic tokens request failed for " + request.file().getName(), cause);
                }
                return;
            }
            if (data == null) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(
                    () -> apply(document, request, data),
                    project.getDisposed());
        });
    }

    private record Range(int start, int end, @NotNull TextAttributesKey key) {
    }

    private void apply(@NotNull Document document, @NotNull Request request, @NotNull SemanticTokensData data) {
        // A newer request follows any edit made since this one was sent.
        if (document.getModificationStamp() != request.stamp() || !request.file().isValid()) {
            return;
        }
        List<Range> ranges = new ArrayList<>();
        data.highlight(request.file(), document, (start, end, key) -> ranges.add(new Range(start, end, key)));

        MarkupModelEx markup = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        List<RangeHighlighter> previous = document.getUserData(HIGHLIGHTERS);
        if (previous != null) {
            for (RangeHighlighter highlighter : previous) {
                if (highlighter.isValid()) {
                    markup.removeHighlighter(highlighter);
                }
            }
        }
        List<RangeHighlighter> added = new ArrayList<>(ranges.size());
        for (Range range : ranges) {
            added.add(markup.addRangeHighlighter(range.key(), range.start(), range.end(),
                    HighlighterLayer.ADDITIONAL_SYNTAX, HighlighterTargetArea.EXACT_RANGE));
        }
        document.putUserData(HIGHLIGHTERS, added);

        // One line per file per session, so idea.log shows whether colouring reached the editor.
        if (document.getUserData(REPORTED) == null) {
            document.putUserData(REPORTED, Boolean.TRUE);
            LOG.info("AngelScript semantic tokens: applied " + added.size() + " highlights to " + request.file().getName());
        }
    }

    @Override
    public void dispose() {
    }
}

package org.jabref.logic.lsp.service;

import java.util.ArrayList;
import java.util.List;

import org.jabref.logic.importer.ParseException;
import org.jabref.logic.importer.fileformat.BibtexParser;
import org.jabref.logic.integrity.IntegrityCheck;
import org.jabref.logic.integrity.IntegrityMessage;
import org.jabref.logic.journals.JournalAbbreviationRepository;
import org.jabref.logic.preferences.CliPreferences;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;

import com.airhacks.afterburner.injection.Injector;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

public class BibtexTextDocumentService implements TextDocumentService {

    private final CliPreferences jabRefCliPreferences;
    private final BibtexParser bibtexParser;
    private final JournalAbbreviationRepository abbreviationRepository;
    private LanguageClient client;

    public BibtexTextDocumentService() {
        jabRefCliPreferences = Injector.instantiateModelOrService(CliPreferences.class);
        bibtexParser = new BibtexParser(jabRefCliPreferences.getImportFormatPreferences());
        abbreviationRepository = new JournalAbbreviationRepository();
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        handleDiagnostics(params.getTextDocument().getUri(), params.getTextDocument().getText(), params.getTextDocument().getVersion());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        handleDiagnostics(params.getTextDocument().getUri(), params.getContentChanges().getFirst().getText(), params.getTextDocument().getVersion());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // client.publishDiagnostics(new PublishDiagnosticsParams(params.getTextDocument().getUri(), new ArrayList<>()));
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }

    private void handleDiagnostics(String uri, String content, int version) {
        List<BibEntry> entries;
        try {
            entries = bibtexParser.parseEntries(content);
        } catch (ParseException e) {
            Diagnostic parseDiagnostic = new Diagnostic(
                    new Range(new Position(0, 0), new Position(0, 1)),
                    "Parse error: " + e.getMessage(),
                    DiagnosticSeverity.Error,
                    "JabRef"
            );
            publishDiagnostics(uri, List.of(parseDiagnostic), version);
            return;
        }

        BibDatabaseContext context = new BibDatabaseContext(new BibDatabase(entries));
        IntegrityCheck integrityCheck = new IntegrityCheck(
                context,
                jabRefCliPreferences.getFilePreferences(),
                jabRefCliPreferences.getCitationKeyPatternPreferences(),
                abbreviationRepository,
                true
        );

        List<Diagnostic> diagnostics = new ArrayList<>();

        for (BibEntry entry : entries) {
            List<IntegrityMessage> messages = integrityCheck.checkEntry(entry);
            for (IntegrityMessage message : messages) {
                diagnostics.add(new Diagnostic(
                        findTextRange(content, entry.getParsedSerialization()),
                        message.message(),
                        DiagnosticSeverity.Warning,
                        "JabRef"
                ));
            }
        }

        publishDiagnostics(uri, diagnostics, version);
    }

    private void publishDiagnostics(String uri, List<Diagnostic> diagnostics, int version) {
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics, version));
    }

    private static Range findTextRange(String content, String searchText) {
        int startOffset = content.indexOf(searchText);
        if (startOffset != -1) {
            int endOffset = startOffset + searchText.length();
            return new Range(offsetToPosition(content, startOffset), offsetToPosition(content, endOffset));
        }
        return new Range(new Position(0, 0), new Position(0, 0));
    }

    private static Position offsetToPosition(String content, int offset) {
        int line = 0;
        int col = 0;
        for (int i = 0; i < offset; i++) {
            if (content.charAt(i) == '\n') {
                line++;
                col = 0;
            } else {
                col++;
            }
        }
        return new Position(line, col);
    }
}

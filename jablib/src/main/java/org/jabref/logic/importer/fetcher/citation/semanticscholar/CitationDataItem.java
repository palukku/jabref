package org.jabref.logic.importer.fetcher.citation.semanticscholar;

/**
 * Used for GSON
 */
public class CitationDataItem {
    private PaperDetails citingPaper;

    public PaperDetails getCitingPaper() {
        return citingPaper;
    }

    public void setCitingPaper(PaperDetails citingPaper) {
        this.citingPaper = citingPaper;
    }
}

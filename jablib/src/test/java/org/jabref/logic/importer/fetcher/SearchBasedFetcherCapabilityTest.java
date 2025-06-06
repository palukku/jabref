package org.jabref.logic.importer.fetcher;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;

import org.jabref.logic.bibtex.FieldPreferences;
import org.jabref.logic.importer.FetcherException;
import org.jabref.logic.importer.ImportCleanup;
import org.jabref.logic.importer.SearchBasedFetcher;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Defines the set of capability tests that each tests a given search capability, e.g. author based search.
 * The idea is to code the capabilities of a fetcher into Java code.
 * This way, a) the capabilities of a fetcher are checked automatically (because they can change from time-to-time by the provider)
 * and b) the queries sent to the fetchers can be debugged directly without a route through to some fetcher code.
 */
interface SearchBasedFetcherCapabilityTest {

    /**
     * Test whether the library API supports author field search.
     */
    @Test
    default void supportsAuthorSearch() throws FetcherException {
        StringJoiner queryBuilder = new StringJoiner("\" AND author:\"", "author:\"", "\"");
        getTestAuthors().forEach(queryBuilder::add);

        List<BibEntry> result = getFetcher().performSearch(queryBuilder.toString());
        FieldPreferences fieldPreferences = mock(FieldPreferences.class);
        when(fieldPreferences.getNonWrappableFields()).thenReturn(FXCollections.observableArrayList());
        ImportCleanup.targeting(BibDatabaseMode.BIBTEX, fieldPreferences).doPostCleanup(result);

        assertFalse(result.isEmpty());
        result.forEach(bibEntry -> {
            String author = bibEntry.getField(StandardField.AUTHOR).orElse("");

            // The co-authors differ, thus we check for the author present at all papers
            getTestAuthors().forEach(expectedAuthor -> assertTrue(author.contains(expectedAuthor.replace("\"", ""))));
        });
    }

    /**
     * Test whether the library API supports year field search.
     */
    @Test
    default void supportsYearSearch() throws FetcherException {
        List<BibEntry> result = getFetcher().performSearch("year:" + getTestYear());
        FieldPreferences fieldPreferences = mock(FieldPreferences.class);
        when(fieldPreferences.getNonWrappableFields()).thenReturn(FXCollections.observableArrayList());
        ImportCleanup.targeting(BibDatabaseMode.BIBTEX, fieldPreferences).doPostCleanup(result);
        List<String> differentYearsInResult = result.stream()
                                                    .map(bibEntry -> bibEntry.getField(StandardField.YEAR))
                                                    .filter(Optional::isPresent)
                                                    .map(Optional::get)
                                                    .distinct()
                                                    .collect(Collectors.toList());

        assertEquals(List.of(getTestYear().toString()), differentYearsInResult);
    }

    /**
     * Test whether the library API supports year range search.
     */
    @Test
    default void supportsYearRangeSearch() throws FetcherException {
        List<String> yearsInYearRange = List.of("2018", "2019", "2020");

        List<BibEntry> result = getFetcher().performSearch("year-range:2018-2020");
        assertFalse(result.isEmpty());

        FieldPreferences fieldPreferences = mock(FieldPreferences.class);
        when(fieldPreferences.getNonWrappableFields()).thenReturn(FXCollections.observableArrayList());
        ImportCleanup.targeting(BibDatabaseMode.BIBTEX, fieldPreferences).doPostCleanup(result);
        List<String> differentYearsInResult = result.stream()
                                                    .map(bibEntry -> bibEntry.getField(StandardField.YEAR))
                                                    .filter(Optional::isPresent)
                                                    .map(Optional::get)
                                                    .distinct()
                                                    .toList();
        assertFalse(result.isEmpty());
        assertTrue(yearsInYearRange.containsAll(differentYearsInResult));
    }

    /**
     * Test whether the library API supports journal based search.
     *
     * WARNING: the error while merging information from user-assigned DOI (more specifically, "10.1016/j.geomphys.2012.09.009")
     * is related to a failed read by the Bibtex Parser (title is formatted in a weird way)
     */
    @Test
    default void supportsJournalSearch() throws FetcherException {
        List<BibEntry> result = getFetcher().performSearch("journal:\"" + getTestJournal() + "\"");
        FieldPreferences fieldPreferences = mock(FieldPreferences.class);
        when(fieldPreferences.getNonWrappableFields()).thenReturn(FXCollections.observableArrayList());
        ImportCleanup.targeting(BibDatabaseMode.BIBTEX, fieldPreferences).doPostCleanup(result);

        assertFalse(result.isEmpty());
        result.forEach(bibEntry -> {
            assertTrue(bibEntry.hasField(StandardField.JOURNAL));
            String journal = bibEntry.getField(StandardField.JOURNAL).orElse("");
            assertTrue(journal.contains(getTestJournal().replace("\"", "")));
        });
    }

    SearchBasedFetcher getFetcher();

    List<String> getTestAuthors();

    String getTestJournal();

    default Integer getTestYear() {
        return 2016;
    }
}

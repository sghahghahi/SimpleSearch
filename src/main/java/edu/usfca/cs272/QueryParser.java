package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Set;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for parsing query files
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class QueryParser {
	/** Maps each query string to a {@code List} of search results */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> exactSearchResults;

	/** Maps each query string to a {@code List} of search results */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> partialSearchResults;

	/** Initialized and populated inverted index object to reference */
	private final InvertedIndex invertedIndex;

	/** Stemmer to use file-wide */
	private final SnowballStemmer snowballStemmer;

	/** Search {@code Function} that will be dynamically assigned */
	private Function<Set<String>, List<InvertedIndex.SearchResult>> searchFunction;

	/** Flag to keep track of current search mode */
	private boolean isExactSearch;

	/**
	 * Constructor that initializes our search result metadata data structure to an empty {@code TreeMap}
	 * @param invertedIndex - The inverted index object to reference. We are not constructing a new inverted index in this class.
	 * This inverted index is passed from the caller and is assumed to be properly initialized and populated
	 */
	public QueryParser(InvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
		this.exactSearchResults = new TreeMap<>();
		this.partialSearchResults = new TreeMap<>();
		setSearchMode(true); // Default to exact search
	}

	/**
	 * Sets the search mode to either exact or partial
	 * @param isExactSearch - The search type. {@code true} represents an exact search, {@code false} represents a partial search
	 */
	public final void setSearchMode(boolean isExactSearch) {
		this.isExactSearch = isExactSearch;
		if (isExactSearch) {
			this.searchFunction = this.invertedIndex::exactSearch;
		} else {
			this.searchFunction = this.invertedIndex::partialSearch;
		}
	}

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation - Where to find the query words
	 * @throws IOException If an IO error occurs
	 */
	public void parseLocation(Path queryLocation) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseLine(line);
			}
		}
	}

	/**
	 * Parses a line and performs a search on the inverted index
	 * @param line The line to parse
	 */
	private void parseLine(String line) {
		Set<String> queryStems = FileStemmer.uniqueStems(line, this.snowballStemmer);
		List<InvertedIndex.SearchResult> searchResults = this.searchFunction.apply(queryStems);

		String queryString = extractQueryString(queryStems);
		if (!queryString.isBlank()) {
			if (isExactSearch) {
				this.exactSearchResults.put(queryString, searchResults);
			} else {
				this.partialSearchResults.put(queryString, searchResults);
			}
		}
	}

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems - The query stems to Stringify
	 * @return The space-separated query {@code String}
	 */
	private String extractQueryString(Set<String> queryStems) {
		return String.join(" ", queryStems);
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location - Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	public void queryJson(Path location) throws IOException {
		if (this.isExactSearch) {
			SearchResultWriter.writeSearchResults(this.exactSearchResults, location);
		} else {
			SearchResultWriter.writeSearchResults(this.partialSearchResults, location);
		}
	}
}

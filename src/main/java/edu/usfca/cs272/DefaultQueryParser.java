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

import java.util.Collections;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for parsing query files
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class DefaultQueryParser implements QueryParser {
	/** Maps each query string to a {@code List} of search results */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> exactSearchResults;

	/** Maps each query string to a {@code List} of search results */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> partialSearchResults;

	/** Initialized and populated inverted index object to reference */
	private final InvertedIndex invertedIndex;

	/** Stemmer to use file-wide */
	private final SnowballStemmer snowballStemmer;

	/** Search {@code Function} that will be dynamically assigned */
	private Function<Set<String>, List<InvertedIndex.SearchResult>> searchMode;

	/** {@code Map} to store either partial or exact search results */
	private TreeMap<String, List<InvertedIndex.SearchResult>> resultMap;

	/** Flag to keep track of current search mode */
	private boolean isExactSearch;

	/**
	 * Constructor that initializes our search result metadata data structure to an empty {@code TreeMap}
	 * @param invertedIndex - The inverted index object to reference. We are not constructing a new inverted index in this class.
	 * This inverted index is passed from the caller and is assumed to be properly initialized and populated
	 */
	public DefaultQueryParser(InvertedIndex invertedIndex) {
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
	@Override
	public final void setSearchMode(boolean isExactSearch) {
		this.isExactSearch = isExactSearch;
		this.searchMode = isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
		this.resultMap = isExactSearch ? this.exactSearchResults : this.partialSearchResults;
	}

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation - Where to find the query words
	 * @throws IOException If an IO error occurs
	 */
	@Override
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
	@Override
	public void parseLine(String line) {
		Set<String> queryStems = FileStemmer.uniqueStems(line, this.snowballStemmer);
		String queryString = QueryParser.extractQueryString(queryStems);

		if (!queryString.isBlank() && !this.resultMap.containsKey(queryString)) {
			List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);
			this.resultMap.put(queryString, searchResults);
		}
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location - Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	@Override
	public void queryJson(Path location) throws IOException {
		SearchResultWriter.writeSearchResults(this.resultMap, location);
	}

	/**
	 * Returns a {@code Set} of the query strings in the result map
	 * @return A {@code Set} of the query strings in the reuslt map
	 */
	@Override
	public Set<String> getQueryStrings() {
		return Collections.unmodifiableSet(this.resultMap.keySet());
	}

	/**
	 * Returns the search type. {@code true} for exact, or {@code false} for partial.
	 * @return the search type. {@code true} for exact, or {@code false} for partial.
	 */
	@Override
	public boolean getSearchType() {
		return this.isExactSearch;
	}

	/**
	 * Returns a {@code List} of {@link InvertedIndex.SearchResult} objects for a particular {@code queryString}
	 * @param queryString The query string to look up in the {@code List} of search results
	 * @return A {@code List} of {@link InvertedIndex.SearchResult} objects for a particular {@code queryString} or an empty list
	 * if the {@code queryString} is not in the {@code Map} of search results
	 */
	@Override
	public List<InvertedIndex.SearchResult> getSearchResults(String queryString) {
		Set<String> queryStems = FileStemmer.uniqueStems(queryString, this.snowballStemmer);
		String joinedQueryString = QueryParser.extractQueryString(queryStems);

		List<InvertedIndex.SearchResult> searchResults = this.resultMap.get(joinedQueryString);

		return searchResults == null ? Collections.emptyList() : Collections.unmodifiableList(searchResults);
	}

	/**
	 * Checks if {@code queryString} is a key in the results map
	 *
	 * @see #getSearchResults(String)
	 *
	 * @param queryString The query string to check
	 * @return {@code true} if {@code queryString} is a key in the result map
	 */
	@Override
	public boolean containsQueryString(String queryString) {
		return getSearchResults(queryString).size() > 0;
	}

	/**
	 * Returns the number of query strings in the reuslt map
	 * @return The number of query strings in the result map
	 */
	@Override
	public int numQueryStrings() {
		return this.resultMap.size();
	}

	/**
	 * Returns the number of search results for a specific query string
	 * @param queryString The query string to look up in the result map
	 * @return The number of search results for {@code queryString}
	 */
	@Override
	public int numSearchResults(String queryString) {
		return getSearchResults(queryString).size();
	}

	@Override
	public String toString() {
		return String.format(
			"Query parser currently has %d %s search %s stored.",
			this.resultMap.size(),
			this.isExactSearch ? "exact" : "partial",
			this.resultMap.size() == 1 ? "result" : "results"
		);
	}
}

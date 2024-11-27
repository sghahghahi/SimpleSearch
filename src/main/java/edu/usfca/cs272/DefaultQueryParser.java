package edu.usfca.cs272;

import java.io.IOException;

import java.nio.file.Path;

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

	@Override
	public final void setSearchMode(boolean isExactSearch) {
		this.isExactSearch = isExactSearch;
		this.searchMode = isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
		this.resultMap = isExactSearch ? this.exactSearchResults : this.partialSearchResults;
	}

	@Override
	public void parseLine(String line) {
		Set<String> queryStems = FileStemmer.uniqueStems(line, this.snowballStemmer);
		String queryString = QueryParser.extractQueryString(queryStems);

		if (!queryString.isBlank() && !this.resultMap.containsKey(queryString)) {
			List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);
			this.resultMap.put(queryString, searchResults);
		}
	}

	@Override
	public void queryJson(Path location) throws IOException {
		SearchResultWriter.writeSearchResults(this.resultMap, location);
	}

	@Override
	public Set<String> getQueryStrings() {
		return Collections.unmodifiableSet(this.resultMap.keySet());
	}

	@Override
	public boolean getSearchType() {
		return this.isExactSearch;
	}

	@Override
	public List<InvertedIndex.SearchResult> getSearchResults(String queryString) {
		Set<String> queryStems = FileStemmer.uniqueStems(queryString, this.snowballStemmer);
		String joinedQueryString = QueryParser.extractQueryString(queryStems);

		List<InvertedIndex.SearchResult> searchResults = this.resultMap.get(joinedQueryString);

		return searchResults == null ? Collections.emptyList() : Collections.unmodifiableList(searchResults);
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

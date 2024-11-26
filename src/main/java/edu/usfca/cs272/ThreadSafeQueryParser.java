package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Set;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

import edu.usfca.cs272.InvertedIndex.SearchResult;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Thread-safe version of {@link DefaultQueryParser}.
 * Uses a work queue to allow a multithreaded parsing process.
 *
 * @author Shyon Ghahghahi
 * @version Fall 2024
 */
public class ThreadSafeQueryParser implements QueryParser {
	/** Maps each query string to a {@code List} of search results */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> exactSearchResults;

	/** Maps each query string to a {@code List} of search results */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> partialSearchResults;

	/** Initialized and populated thread-safe inverted index to reference */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** Search {@code Function} that will be dynamically assigned */
	private Function<Set<String>, List<InvertedIndex.SearchResult>> searchMode;

	/** {@code Map} to store either partial or exact search results */
	private TreeMap<String, List<InvertedIndex.SearchResult>> resultMap;

	/** Flag to keep track of current search mode */
	private volatile boolean isExactSearch;

	/** The work queue to assign tasks to */
	private final WorkQueue queue;

	/**
	 * Constructor that initializes our search result metadata data tructure to an empty {@code TreeMap}
	 * @param invertedIndex The populated inverted index object to reference
	 * @param queue The work queue to assign tasks to
	 */
	public ThreadSafeQueryParser(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
		this.exactSearchResults = new TreeMap<>();
		this.partialSearchResults = new TreeMap<>();
		this.invertedIndex = invertedIndex;
		this.queue = queue;
		setSearchMode(true);
	}

	/** Nested class that represents a task for a thread to do */
	private class Work implements Runnable {
		/** The line to parse */
		private final String line;

		/**
		 * Constructs a new task
		 * @param line The line to parse
		 */
		public Work(String line) {
			this.line = line;
		}

		@Override
		public void run() {
			parseLine(line);
		}
	}

	@Override
	public final void setSearchMode(boolean isExactSearch) {
		// No need to synchronize because of volatile keyword
		this.isExactSearch = isExactSearch;

		synchronized (this) {
			this.searchMode = isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
			this.resultMap = isExactSearch ? this.exactSearchResults : this.partialSearchResults;
		}
	}

	@Override
	public void parseLocation(Path queryLocation) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				this.queue.execute(new Work(line));
			}
		} finally {
			this.queue.finish();
		}
		
		/* TODO 
		QueryParser.super.parseLocation(queryLocation);
		this.queue.finish();
		*/
	}

	@Override
	public void parseLine(String line) {
		// TODO create tasks, move the implementaiton into run
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
		Set<String> queryStems = FileStemmer.uniqueStems(line, snowballStemmer);
		String queryString = QueryParser.extractQueryString(queryStems);

		synchronized (this.resultMap) {
			if (queryString.isBlank() || this.resultMap.containsKey(queryString)) {
				return;
			}
		}

		List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);

		synchronized (this.resultMap) {
			this.resultMap.put(queryString, searchResults);
		}
	}

	@Override
	public void queryJson(Path location) throws IOException {
		synchronized (this.resultMap) {
			SearchResultWriter.writeSearchResults(this.resultMap, location);
		}
	}

	@Override
	public Set<String> getQueryStrings() {
		synchronized (this.resultMap) {
			return Collections.unmodifiableSet(this.resultMap.keySet());
		}
	}

	@Override
	public boolean getSearchType() {
		// No need to synchronize because of volatile keyword
		return this.isExactSearch;
	}

	@Override
	public List<SearchResult> getSearchResults(String queryString) {
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
		Set<String> queryStems = FileStemmer.uniqueStems(queryString, snowballStemmer);
		String joinedQuerySring = QueryParser.extractQueryString(queryStems);

		List<InvertedIndex.SearchResult> searchResults = null;
		synchronized (this.resultMap) {
			searchResults = this.resultMap.get(joinedQuerySring);
		}

		return searchResults == null ? Collections.emptyList() : Collections.unmodifiableList(searchResults);
	}

	@Override
	public int numQueryStrings() {
		synchronized (this.resultMap) {
			return this.resultMap.size();
		}
	}

	@Override
	public String toString() {
		synchronized (this.resultMap) {
			return String.format(
				"Query parser currently has %d %s search %s stored.",
				this.resultMap.size(),
				this.isExactSearch ? "exact" : "partial",
				this.resultMap.size() == 1 ? "result" : "results"
			);
		}
	}
}

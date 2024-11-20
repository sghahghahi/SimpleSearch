package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Set;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/*
 * TODO Try to create a common interface and move as much as possible in to the interface
 */

/**
 * Thread-safe version of {@link QueryParser}.
 * Uses a work queue to allow a multithreaded parsing process.
 *
 * @author Shyon Ghahghahi
 * @version Fall 2024
 */
public class ThreadSafeQueryParser {
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

	/**
	 * Sets the search mode to either exact or partial
	 * @param isExactSearch The search type. {@code true} represents an exact search, {@code false} represents a partial search
	 */
	public synchronized final void setSearchMode(boolean isExactSearch) {
		this.searchMode = isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
		this.resultMap = isExactSearch ? this.exactSearchResults : this.partialSearchResults;
	}

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation Where to find the query words
	 * @throws IOException If an IO error occurs
	 */
	public void parseLocation(Path queryLocation) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				this.queue.execute(new Work(line));
			}
		} finally {
			this.queue.finish();
			this.queue.shutdown(); // TODO shutdown should happen in Driver
		}
	}

	/**
	 * Parses a line and performs a search on the inverted index
	 * @param line The line to parse
	 */
	public void parseLine(String line) {
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
		Set<String> queryStems = FileStemmer.uniqueStems(line, snowballStemmer);
		String queryString = extractQueryString(queryStems);

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

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems The query stems to Stringify
	 * @return The space-separated query {@code String}
	 */
	private static String extractQueryString(Set<String> queryStems) { // TODO Remove
		return String.join(" ", queryStems);
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	public synchronized void queryJson(Path location) throws IOException {
		SearchResultWriter.writeSearchResults(this.resultMap, location);
	}
	
	/*
	 * TODO Make sure to consistently synchronize on the resultMap
	 */
}

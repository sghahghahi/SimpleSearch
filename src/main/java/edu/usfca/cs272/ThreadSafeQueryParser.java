package edu.usfca.cs272;

import edu.usfca.cs272.MultiReaderLock.SimpleLock;

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

	/** The lock used to protect concurrent access to the underlying instance members */
	private final MultiReaderLock lock; // TODO Don't get a lot of benefit for our tests

	/** The conditional lock used for writing */
	private final SimpleLock writeLock;

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
		this.lock = new MultiReaderLock();
		this.writeLock = this.lock.writeLock();
		this.queue = queue;
		setSearchMode(true);
	}

	/** Nested class that represents a task for a thread to do */
	private static class Work implements Runnable { // TODO non-static
		/** The line to parse */
		private final String line;

		/** The parser to use to parse the line */
		private final ThreadSafeQueryParser parser;

		/**
		 * Constructs a new task
		 * @param line The line to parse
		 * @param parsre The parser to use to parse the line
		 */
		public Work(String line, ThreadSafeQueryParser parser) {
			this.line = line;
			this.parser = parser;
		}

		@Override
		public void run() {
			this.parser.parseLine(line);
		}
	}

	/**
	 * Sets the search mode to either exact or partial
	 * @param exactSearch The search type. {@code true} represents an exact search, {@code false} represents a partial search
	 */
	public final void setSearchMode(boolean isExactSearch) {
		this.writeLock.lock();
		try {
			this.searchMode = isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
			this.resultMap = isExactSearch ? this.exactSearchResults : this.partialSearchResults;
		} finally {
			this.writeLock.unlock();
		}
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
				Work work = new Work(line, this);
				this.queue.execute(work);
			}
		} finally {
			this.queue.finish();
			this.queue.shutdown();
		}
	}

	/**
	 * Parses a line and performs a search on the inverted index
	 * @param line The line to parse
	 */
	private void parseLine(String line) { // TODO Still needs to be public
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
		Set<String> queryStems = FileStemmer.uniqueStems(line, snowballStemmer);

		List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);

		String queryString = extractQueryString(queryStems);
		if (!queryString.isBlank()) { // TODO No longer checking containsKey
			synchronized (this.resultMap) { // TODO Choose a single means of synchronization
				this.resultMap.put(queryString, searchResults);
			}
		}

		/* TODO
		synchronized (this.resultMap) {
			if (queryString.isBlank() || this.resultMap.containsKey(queryString)) {
				return;
			}
		}

		List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);

		synchronized (this.resultMap) {
			this.resultMap.put(queryString, searchResults);
		}
		*/

	}

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems The query stems to Stringify
	 * @return The space-separated query {@code String}
	 */
	private static String extractQueryString(Set<String> queryStems) {
		return String.join(" ", queryStems);
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	public void queryJson(Path location) throws IOException {
		SearchResultWriter.writeSearchResults(this.resultMap, location); // TODO Also sync this too
	}
}

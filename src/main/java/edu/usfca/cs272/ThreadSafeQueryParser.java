package edu.usfca.cs272;

import edu.usfca.cs272.MultiReaderLock.SimpleLock;

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
 * TODO
 */
public class ThreadSafeQueryParser {
	/** TODO */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> exactSearchResults;

	/** TODO */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> partialSearchResults;

	/** TODO */
	private final InvertedIndex invertedIndex;

	/** TODO */
	private final SnowballStemmer snowballStemmer;

	/** TODO */
	private Function<Set<String>, List<InvertedIndex.SearchResult>> searchMode;

	/** TODO */
	private TreeMap<String, List<InvertedIndex.SearchResult>> resultMap;

	/** TODO */
	// Volatile?
	private boolean isExactSearch;

	/** TODO */
	private final MultiReaderLock lock;

	/** TODO */
	private final SimpleLock readLock;

	/** TODO */
	private final SimpleLock writeLock;

	/** TODO */
	// Does this need to be class-level?
	private final WorkQueue queue;

	/**
	 * TODO
	 * @param invertedIndex
	 */
	public ThreadSafeQueryParser(InvertedIndex invertedIndex, int numThreads) {
		this.invertedIndex = invertedIndex;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
		this.exactSearchResults = new TreeMap<>();
		this.partialSearchResults = new TreeMap<>();
		this.lock = new MultiReaderLock();
		this.readLock = this.lock.readLock();
		this.writeLock = this.lock.writeLock();
		this.queue = new WorkQueue(numThreads);
		setSearchMode(true);
	}

	/** Nested class that represents a task for a thread to do */
	/** TODO */
	private static class Work implements Runnable {
		/** TODO */
		private final String line;

		/** TODO */
		private final ThreadSafeQueryParser parser;

		/**
		 * Constructs a new task
		 * TODO - params
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
	 * TODO
	 * @param exactSearch
	 */
	public void setSearchMode(boolean exactSearch) {
		this.writeLock.lock();
		try {
			this.isExactSearch = exactSearch;
			this.searchMode = this.isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
			this.resultMap = this.isExactSearch ? this.exactSearchResults : this.partialSearchResults;
		} finally {
			this.writeLock.unlock();
		}
	}

	/**
	 * TODO
	 * @param queryLocation
	 * @throws IOException
	 */
	public void parseLocation(Path queryLocation) throws IOException {
		this.readLock.lock();
		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				Work work = new Work(line, this);
				this.queue.execute(work);
			}

		} finally {
			this.readLock.unlock();
		}

		this.queue.join();
	}

	/**
	 * TODO
	 * @param line
	 */
	private void parseLine(String line) {
		this.readLock.lock();
		try {
			Set<String> queryStems = FileStemmer.uniqueStems(line, this.snowballStemmer);
			List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);

			String queryString = extractQueryString(queryStems);
			if (!queryString.isBlank()) {
				this.resultMap.put(queryString, searchResults);
			}
		} finally {
			this.readLock.unlock();
		}
	}

	/**
	 * TODO
	 * @param queryStems
	 * @return
	 */
	public String extractQueryString(Set<String> queryStems) {
		this.readLock.lock();
		try {
			return String.join(" ", queryStems);
		} finally {
			this.readLock.lock();
		}
	}
}

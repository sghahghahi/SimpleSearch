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
	private Function<Set<String>, List<InvertedIndex.SearchResult>> searchMode;

	/** TODO */
	private TreeMap<String, List<InvertedIndex.SearchResult>> resultMap;

	/** TODO */
	private final MultiReaderLock lock;

	/** TODO */
	private final SimpleLock writeLock;

	/** TODO */
	private final WorkQueue queue;

	/**
	 * TODO
	 * @param invertedIndex
	 */
	public ThreadSafeQueryParser(InvertedIndex invertedIndex, int numThreads) {
		this.invertedIndex = invertedIndex;
		this.exactSearchResults = new TreeMap<>();
		this.partialSearchResults = new TreeMap<>();
		this.lock = new MultiReaderLock();
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
	public void setSearchMode(boolean isExactSearch) {
		this.writeLock.lock();
		try {
			this.searchMode = isExactSearch ? this.invertedIndex::exactSearch : this.invertedIndex::partialSearch;
			this.resultMap = isExactSearch ? this.exactSearchResults : this.partialSearchResults;
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
		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				Work work = new Work(line, this);
				this.queue.execute(work);
			}

		}

		this.queue.join();
	}

	/**
	 * TODO
	 * @param line
	 */
	private void parseLine(String line) {
		SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);
		Set<String> queryStems = FileStemmer.uniqueStems(line, stemmer);
		List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);

		String queryString = extractQueryString(queryStems);
		if (!queryString.isBlank()) {
			this.writeLock.lock();
			try {
				this.resultMap.put(queryString, searchResults);
			} finally {
				this.writeLock.unlock();
			}
		}
	}

	/**
	 * TODO
	 * @param queryStems
	 * @return
	 */
	private static String extractQueryString(Set<String> queryStems) {
		return String.join(" ", queryStems);
	}

	/**
	 * TODO
	 * @param location
	 * @throws IOException
	 */
	public synchronized void queryJson(Path location) throws IOException {
		SearchResultWriter.writeSearchResults(this.resultMap, location);
	}
}

package edu.usfca.cs272;

import edu.usfca.cs272.MultiReaderLock.SimpleLock;

import java.io.IOException;

import java.nio.file.Path;

import java.util.Set;
import java.util.ArrayList;
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
	public ThreadSafeQueryParser(InvertedIndex invertedIndex, WorkQueue queue) {
		this.exactSearchResults = new TreeMap<>();
		this.partialSearchResults = new TreeMap<>();
		this.invertedIndex = invertedIndex;
		this.lock = new MultiReaderLock();
		this.writeLock = this.lock.writeLock();
		this.queue = queue;
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
		ArrayList<String> lines = FileStemmer.listStems(queryLocation);
		for (String line : lines) {
			Work work = new Work(line, this);
			this.queue.execute(work);
		}
	}

	/**
	 * TODO
	 * @param line
	 */
	private void parseLine(String line) {
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
		Set<String> queryStems = FileStemmer.uniqueStems(line, snowballStemmer); // Give stemmer to each thread to reduce synchronized blocks

		List<InvertedIndex.SearchResult> searchResults = this.searchMode.apply(queryStems);

		String queryString = extractQueryString(queryStems);
		if (!queryString.isBlank()) {
			synchronized (this.resultMap) {
				this.resultMap.put(queryString, searchResults);
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
	public void queryJson(Path location) throws IOException {
		SearchResultWriter.writeSearchResults(this.resultMap, location);
	}
}

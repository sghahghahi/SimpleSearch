package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import edu.usfca.cs272.MultiReaderLock.SimpleLock;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** TODO */
public class ThreadSafeTextFileIndexer {
	/** TOdO */
	private final InvertedIndex invertedIndex;

	/** TODO */
	// Simliar to search, give each thread a stemmer?
	private final SnowballStemmer snowballStemmer;

	/** TODO */
	private final MultiReaderLock lock;

	/** TODO */
	private final SimpleLock readLock;

	/** TODO */
	private final SimpleLock writeLock;

	/** TODO */
	private final WorkQueue queue;

	/**
	 * TODO
	 * @param invertedIndex
	 * @param numThreads
	 */
	public ThreadSafeTextFileIndexer(InvertedIndex invertedIndex, int numThreads) {
		this.invertedIndex = invertedIndex;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
		this.lock = new MultiReaderLock();
		this.readLock = this.lock.readLock();
		this.writeLock = this.lock.writeLock();
		this.queue = new WorkQueue(numThreads);
	}
}

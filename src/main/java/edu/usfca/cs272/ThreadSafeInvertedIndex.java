package edu.usfca.cs272;

import java.util.List;
import java.util.Set;

import edu.usfca.cs272.MultiReaderLock.SimpleLock;

/**
 * TODO
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/** TODO */
	private final MultiReaderLock lock;

	/** TODO */
	private final SimpleLock readLock;

	/** TODO */
	private final SimpleLock writeLock;

	/**
	 * TODO
	 */
	public ThreadSafeInvertedIndex() {
		super();
		this.lock = new MultiReaderLock();
		this.readLock = this.lock.readLock();
		this.writeLock = this.lock.writeLock();
	}

	/*
	 * Things to override:
	 * Anything related to search
	 * exactSearch()
	 * partialSearch()
	 * Set search mode
	 * Any read/write methods that any of the bove 3 methods call
	 */

	@Override
	public List<SearchResult> exactSearch(Set<String> queryStems) {
		readLock.lock();
		try {
			return super.exactSearch(queryStems);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public List<SearchResult> partialSearch(Set<String> queryStems) {
		readLock.lock();
		try {
			return super.partialSearch(queryStems);
		} finally {
			readLock.unlock();
		}
	}
}

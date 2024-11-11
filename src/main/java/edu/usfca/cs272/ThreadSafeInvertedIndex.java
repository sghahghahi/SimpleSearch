package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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

	/** TODO */
	@Override
	public List<InvertedIndex.SearchResult> exactSearch(Set<String> queryStems) {
		this.readLock.lock();
		try {
			return super.exactSearch(queryStems);
		} finally {
			this.readLock.unlock();
		}
	}

	/** TODO */
	@Override
	public List<InvertedIndex.SearchResult> partialSearch(Set<String> queryStems) {
		this.readLock.lock();
		try {
			return super.partialSearch(queryStems);
		} finally {
			this.readLock.unlock();
		}
	}

	/** TODO */
	@Override
	public int addWords(List<String> stemmedWords, String location, int wordPosition) {
		this.writeLock.lock();
		try {
			return super.addWords(stemmedWords, location, wordPosition);
		} finally {
			this.writeLock.unlock();
		}
	}

	/** TODO */
	@Override
	public boolean addWordPosition(String word, String location, int wordPosition) {
		return super.addWordPosition(word, location, wordPosition);
	}

	/** TODO */
	@Override
	public Map<String, Integer> getCounts() {
		this.readLock.lock();
		try {
			return super.getCounts();
		} finally {
			this.readLock.unlock();
		}
	}

	/** TODO */
	@Override
	public Set<String> getStemLocations() {
		this.readLock.lock();
		try {
			return super.getStemLocations();
		} finally {
			this.readLock.unlock();
		}
	}

	/** TODO */
	@Override
	public int numCounts() {
		this.readLock.lock();
		try {
			return super.numCounts();
		} finally {
			this.readLock.unlock();
		}
	}

	/** TODO */
	@Override
	public void indexJson(Path location) throws IOException {
		this.readLock.lock();
		try {
			super.indexJson(location);
		} finally {
			this.readLock.unlock();
		}
	}
}

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

	/** TODO */
	public class SearchResult extends InvertedIndex.SearchResult {
		/** TODO */
		private final MultiReaderLock lock;

		/** TODO */
		private final SimpleLock readLock;

		/** TODO */
		private final SimpleLock writeLock;

		/** TODO */
		public SearchResult(String location) {
			super(location);
			this.lock = new MultiReaderLock();
			this.readLock = this.lock.readLock();
			this.writeLock = this.lock.writeLock();
		}

		/** TODO */
		@Override
		public int getCount() {
			this.readLock.lock();
			try {
				return super.getCount();
			} finally {
				this.readLock.unlock();
			}
		}

		/** TODO */
		@Override
		public double getScore() {
			this.writeLock.lock();
			try {
				return super.getScore();
			} finally {
				this.writeLock.unlock();
			}
		}

		/** TODO */
		@Override
		public String getLocation() {
			this.writeLock.lock();
			try {
				return super.getLocation();
			} finally {
				this.writeLock.unlock();
			}
		}
	}

	@Override
	public List<InvertedIndex.SearchResult> exactSearch(Set<String> queryStems) {
		this.readLock.lock();
		try {
			return super.exactSearch(queryStems);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public List<InvertedIndex.SearchResult> partialSearch(Set<String> queryStems) {
		this.readLock.lock();
		try {
			return super.partialSearch(queryStems);
		} finally {
			this.readLock.unlock();
		}
	}
}

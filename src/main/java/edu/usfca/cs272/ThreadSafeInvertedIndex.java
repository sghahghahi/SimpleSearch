package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usfca.cs272.MultiReaderLock.SimpleLock;

/**
 * Thread-safe version of {@link InvertedIndex}
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
	/** The lock used to protect concurrent access to the underlying instance members */
	private final MultiReaderLock lock;

	/** The conditional lock used for reading */
	private final SimpleLock readLock;

	/** The conditional lock used for writing */
	private final SimpleLock writeLock;

	/**
	 * Initializes a new read lock and write lock
	 */
	public ThreadSafeInvertedIndex() {
		super();
		this.lock = new MultiReaderLock();
		this.readLock = this.lock.readLock();
		this.writeLock = this.lock.writeLock();
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

	@Override
	public int addWords(List<String> stemmedWords, String location, int wordPosition) {
		this.writeLock.lock();
		try {
			return super.addWords(stemmedWords, location, wordPosition);
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public boolean addWordPosition(String word, String location, int wordPosition) {
		this.writeLock.lock();
		try {
			return super.addWordPosition(word, location, wordPosition);
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex indexToAdd) {
		this.writeLock.lock();
		try {
			super.addAll(indexToAdd);
		} finally {
			this.writeLock.unlock();
		}
	}

	@Override
	public Map<String, Integer> getCounts() {
		this.readLock.lock();
		try {
			return super.getCounts();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public Set<String> getStemLocations() {
		this.readLock.lock();
		try {
			return super.getStemLocations();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public int numCounts() {
		this.readLock.lock();
		try {
			return super.numCounts();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public int numStems(String location) {
		this.readLock.lock();
		try {
			return super.numStems(location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		this.readLock.lock();
		try {
			return super.isEmpty();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public boolean containsLocation(String location) {
		this.readLock.lock();
		try {
			return super.containsLocation(location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public void indexCounts(Path location) throws IOException {
		this.readLock.lock();
		try {
			super.indexCounts(location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public void indexJson(Path location) throws IOException {
		this.readLock.lock();
		try {
			super.indexJson(location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public int numLocations(String word) {
		this.readLock.lock();
		try {
			return super.numLocations(word);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public int numPositions(String word, String location) {
		this.readLock.lock();
		try {
			return super.numPositions(word, location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public int numWords() {
		this.readLock.lock();
		try {
			return super.numWords();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public Set<String> getLocations(String word) {
		this.readLock.lock();
		try {
			return super.getLocations(word);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public Set<Integer> getPositions(String word, String location) {
		this.readLock.lock();
		try {
			return super.getPositions(word, location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public Set<String> getWords() {
		this.readLock.lock();
		try {
			return super.getWords();
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public boolean containsWord(String word) {
		this.readLock.lock();
		try {
			return super.containsWord(word);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public boolean containsLocation(String word, String location) {
		this.readLock.lock();
		try {
			return super.containsLocation(word, location);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public boolean containsPosition(String word, String location, int position) {
		this.readLock.lock();
		try {
			return super.containsPosition(word, location, position);
		} finally {
			this.readLock.unlock();
		}
	}

	@Override
	public String toString() {
		this.readLock.lock();
		try {
			return super.toString();
		} finally {
			this.readLock.unlock();
		}
	}
}

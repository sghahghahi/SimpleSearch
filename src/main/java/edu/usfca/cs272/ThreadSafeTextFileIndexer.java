package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

/** Thread-safe version of {@link TextFileIndexer} */
public class ThreadSafeTextFileIndexer extends TextFileIndexer {
	/** {@link InvertedIndex} object to reference class-wide */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** The work queue to assign tasks to */
	private final WorkQueue queue;

	/**
	 * Instantiates this class with an {@link InvertedIndex} object to reference
	 * @param invertedIndex The {@link InvertedIndex} object to reference
	 * @param queue The work queue to assign tasks to
	 */
	public ThreadSafeTextFileIndexer(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
		super(invertedIndex);
		this.invertedIndex = invertedIndex;
		this.queue = queue;
	}

	/** Nested class that represents a task for a thread to do */
	private class Work implements Runnable {
		/** The file location to index */
		private final Path location;

		/**
		 * Constructs a new task
		 * @param location The file location to index
		 */
		public Work(Path location) {
			this.location = location;
		}

		@Override
		public void run() {
			try {
				InvertedIndex localIndex = new InvertedIndex();
				TextFileIndexer.indexFile(this.location, localIndex);
				invertedIndex.addAll(localIndex);
			} catch (IOException e) {
				System.err.println("IOException occured during run() method.");
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Reads file from {@code path}.
	 * @param path File path to read from
	 * @throws IOException If an IO error occurs
	 */
	public void indexFile(Path path) throws IOException {
		this.queue.execute(new Work(path));;
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code location} to its appropriate method.
	 * @param location The location of either a directory of file
	 * @throws IOException If an IO error occurs
	 */
	public void indexLocation(Path location) throws IOException {
		super.indexLocation(location);
		this.queue.finish();
	}
}

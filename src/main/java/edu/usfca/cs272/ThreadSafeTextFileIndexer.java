package edu.usfca.cs272;

import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
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
		Path location; // TODO keywords

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
				/* TODO 
				InvertedIndex localIndex = new InvertedIndex();
				TextFileIndexer.indexFile(path, localIndex);
				this.invertedIndex.addAll(localIndex);
				*/

				indexFile(this.location);
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
		// TODO create a task and add to the queue here
		InvertedIndex localIndex = new InvertedIndex();
		TextFileIndexer.indexFile(path, localIndex);
		this.invertedIndex.addAll(localIndex);
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirLocation}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirLocation Location of directory to traverse
	 * @throws IOException If an IO error occurs
	 */
	public void indexDirectory(Path dirLocation) throws IOException { // TODO Remove
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirLocation)) {
			for (Path location : dirStream) {
				if (Files.isDirectory(location)) {
					indexDirectory(location);
				} else if (TextFileIndexer.isTextFile(location)) {
					this.queue.execute(new Work(location));
				}
			}
		}
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code location} to its appropriate method.
	 * @param location The location of either a directory of file
	 * @throws IOException If an IO error occurs
	 */
	public void indexLocation(Path location) throws IOException {
		try {
			// TODO super.indexLocation(location);
			if (Files.isDirectory(location)) {
				indexDirectory(location);
			} else {
				Work work = new Work(location);
				this.queue.execute(work);
			}
		} finally {
			this.queue.finish();
		}
	}
}

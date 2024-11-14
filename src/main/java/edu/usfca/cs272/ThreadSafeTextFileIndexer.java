package edu.usfca.cs272;

import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;

/** Thread-safe version of {@link TextFileIndexer} */
public class ThreadSafeTextFileIndexer {
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
		this.invertedIndex = invertedIndex;
		this.queue = queue;
	}

	/** Nested class that represents a task for a thread to do */
	private class Work implements Runnable {
		/** The file location to index */
		Path location;

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
		String location = path.toString();

		/* TODO Start simple with addAll so we have a comparison point
		InvertedIndex localIndex = new InvertedIndex();
		TextFileIndexer.indexFile(path, localIndex);
		this.invertedIndex.addAll(localIndex);
		*/

		ArrayList<String> words = FileStemmer.listStems(path);
		this.invertedIndex.addWords(words, location, 1);
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirLocation}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirLocation Location of directory to traverse
	 * @throws IOException If an IO error occurs
	 */
	public void indexDirectory(Path dirLocation) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirLocation)) {
			for (Path location : dirStream) {
				if (Files.isDirectory(location)) {
					indexDirectory(location);
				} else if (TextFileIndexer.isTextFile(location)) {
					Work work = new Work(location);
					this.queue.execute(work);
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

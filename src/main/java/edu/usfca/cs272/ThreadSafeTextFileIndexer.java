package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/** TODO */
public class ThreadSafeTextFileIndexer {
	/** TODO */
	private final InvertedIndex invertedIndex;

	/** TODO */
	private final SnowballStemmer snowballStemmer;

	/** TODO */
	private final WorkQueue queue;

	/**
	 * TODO
	 * @param invertedIndex
	 * @param numThreads
	 */
	public ThreadSafeTextFileIndexer(InvertedIndex invertedIndex, WorkQueue queue) {
		this.invertedIndex = invertedIndex;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
		this.queue = queue;
	}

	private static class Work implements Runnable {
		/** TODO */
		Path location;

		/** TODO */
		ThreadSafeTextFileIndexer indexer;

		/**
		 * TODO
		 * @param location
		 * @param threadSafeTextFileIndexer
		 */
		public Work(Path location, ThreadSafeTextFileIndexer indexer) {
			this.location = location;
			this.indexer = indexer;
		}

		/** TODO */
		@Override
		public void run() {
			try {
				this.indexer.indexFile(location);
			} catch (IOException e) {
				System.err.println("IOException occured during run() method.");
				Thread.currentThread().interrupt();
			}
		}

	}

	/**
	 * TODO
	 * @param path
	 * @throws IOException
	 */
	public void indexFile(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			int wordPosition = 1;
			String line = null;
			String location = path.toString();

			while ((line = reader.readLine()) != null) {
				String[] cleanedWords = FileStemmer.parse(line);

				for (String cleanWord : cleanedWords) {
					String stemmedWord;
					synchronized (this.snowballStemmer) {
						stemmedWord = this.snowballStemmer.stem(cleanWord).toString();
					}
					synchronized (this.invertedIndex) {
						this.invertedIndex.addWordPosition(stemmedWord, location, wordPosition++);
					}
				}
			}
		}
	}

	/**
	 * TODO
	 * @param dirLocation
	 * @throws IOException
	 */
	public void indexDirectory(Path dirLocation) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirLocation)) {
			for (Path location : dirStream) {
				if (Files.isDirectory(location)) {
					indexDirectory(location);
				} else if (isTextFile(location)) {
					Work work = new Work(location, this);
					this.queue.execute(work);
				}
			}
		}
	}

	/**
	 * TODO
	 * @param location
	 * @return
	 */
	public static boolean isTextFile(Path location) {
		String lowerCaseLocation = location.toString().toLowerCase();

		return (
			lowerCaseLocation.endsWith(".txt") ||
			lowerCaseLocation.endsWith(".text")
		);
	}

	/**
	 * TODO
	 * @param location
	 * @throws IOException
	 */
	public void indexLocation(Path location) throws IOException {
		if (Files.isDirectory(location)) {
			indexDirectory(location);
		} else {
			Work work = new Work(location, this);
			this.queue.execute(work);
		}
	}
}

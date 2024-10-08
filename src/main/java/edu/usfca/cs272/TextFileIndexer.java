package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/** Class that handles anything I/O related. All methods are {@code static}. */
public class TextFileIndexer {

	/** No need to instantiate this class because all methods are {@code static} */
	private TextFileIndexer() {}

	/**
	 * Reads file from {@code path}.
	 * Adds {@code path} and word counts to {@code this.wordStems} to be written to a file later.
	 * @param path File path to read from
	 * @param invertedIndex The {@code InvertedIndex} object that requires I/O operations
	 * @throws IOException If an IO error occurs
	 */
	public static void indexFile(Path path, InvertedIndex invertedIndex) throws IOException {
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);

		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			int wordPosition = 1;
			String line = null;
			String location = path.toString();

			while ((line = reader.readLine()) != null) {
				String[] cleanedWords = FileStemmer.parse(line);

				for (String cleanWord : cleanedWords) {
					invertedIndex.addWordPosition(
						snowballStemmer.stem(cleanWord).toString(),
						location, wordPosition++
					);
				}

				invertedIndex.addCount(location, cleanedWords.length); // TODO Okay but could update once
			}
			
			// TODO Could update the count once here based on the wordPosition (might be off by one)
		}
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirLocation}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirLocation location of directory to traverse
	 * @param invertedIndex The {@code InvertedIndex} object that requires I/O operations
	 * @throws IOException If an IO error occurs
	 */
	public static void indexDirectory(Path dirLocation, InvertedIndex invertedIndex) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirLocation)) {
			for (Path location : dirStream) {
				if (Files.isDirectory(location)) {
					indexDirectory(location, invertedIndex);
				} else {
					if (isTextFile(location)) {
						indexFile(location, invertedIndex);
					}
				}
			}
		}
	}

	/**
	 * Checks if the file at {@code location} ends in either {@code .txt} or {@code .text} (case-insensitive)
	 *
	 * @param location The file location to check
	 * @return {@code true} if the file at {@code location} ends with {@code .txt} or {@code .text} (case-insensitive).
	 */
	public static boolean isTextFile(Path location) {
		String lowerCaseLocation = location.toString().toLowerCase();

		return (
			lowerCaseLocation.endsWith(".txt") ||
			lowerCaseLocation.endsWith(".text")
		);
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code location} to its appropriate method.
	 * @param location The location of either a directory or file
	 * @param invertedIndex The {@code InvertedIndex} object that requires I/O operations
	 * @throws IOException If an IO error occurs
	 */
	public static void indexLocation(Path location, InvertedIndex invertedIndex) throws IOException {
		if (Files.isDirectory(location)) {
			indexDirectory(location, invertedIndex);
		} else {
			indexFile(location, invertedIndex);
		}
	}
}

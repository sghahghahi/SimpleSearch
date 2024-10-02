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
			// TODO String location = path.toString();

			while ((line = reader.readLine()) != null) {
				String[] cleanedWords = FileStemmer.parse(line);

				for (String cleanWord : cleanedWords) {
					invertedIndex.addWordPosition(
						snowballStemmer.stem(cleanWord).toString(),
						path.toString(), wordPosition++
					);
				}

				invertedIndex.addCount(path.toString(), cleanedWords.length);
			}
		}
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirPath}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirPath path of directory to traverse
	 * @param invertedIndex The {@code InvertedIndex} object that requires I/O operations
	 * @throws IOException If an IO error occurs
	 */
	public static void indexDirectory(Path dirPath, InvertedIndex invertedIndex) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
			for (Path path : dirStream) {
				if (Files.isDirectory(path)) {
					indexDirectory(path, invertedIndex);
				} else {
					if (isTextFile(path)) {
						indexFile(path, invertedIndex);
					}
				}
			}
		}
	}

	/**
	 * Checks if the file at {@code path} ends in either {@code .txt} or {@code .text} (case-insensitive)
	 *
	 * @param path The file path to check
	 * @return {@code true} if the file at {@code path} ends with {@code .txt} or {@code .text} (case-insensitive).
	 */
	public static boolean isTextFile(Path path) {
		String lowerCasePath = path.toString().toLowerCase();

		return (
			lowerCasePath.endsWith(".txt") ||
			lowerCasePath.endsWith(".text")
		);
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code path} to its appropriate method.
	 * @param path The path of either a directory or file
	 * @param invertedIndex The {@code InvertedIndex} object that requires I/O operations
	 * @throws IOException If an IO error occurs
	 */
	public static void indexPath(Path path, InvertedIndex invertedIndex) throws IOException {
		if (Files.isDirectory(path)) {
			indexDirectory(path, invertedIndex);
		} else {
			indexFile(path, invertedIndex);
		}
	}
}

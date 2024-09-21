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
public class IOHandler {

	/** No need to instantiate this class because all methods are {@code static} */
	private IOHandler() {}

	/**
	 * Reads file from {@code path}.
	 * Adds {@code path} and word counts to {@code this.wordStems} to be written to a file later.
	 * @param path File path to read from
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	public static void readFile(Path path, InvertedIndex invertedIndex) throws IOException {
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);

		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				invertedIndex.addWordCounts(line, snowballStemmer, path);
				invertedIndex.buildInvertedIndex(line, snowballStemmer, path);
			}
		}
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirPath}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirPath path of directory to traverse
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	public static void readDir(Path dirPath, InvertedIndex invertedIndex) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
			for (Path path : dirStream) {
				if (Files.isDirectory(path)) {
					readDir(path, invertedIndex);
				} else {
					if (isTextFile(path)) {
						// Reset word position to 1 every time we read from a new file
						invertedIndex.wordPosition = 1;
						readFile(path, invertedIndex);
					}
				}
			}
		}
	}

	/**
	 * @param path The file path to check
	 * @return If the file at {@code path} ends with {@code .txt} or {@code .text} (case-insensitive).
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
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	public static void textFlag(Path path, InvertedIndex invertedIndex) throws IOException {
		if (Files.isDirectory(path)) {
			readDir(path, invertedIndex);
		} else {
			readFile(path, invertedIndex);
		}
	}

	/**
	 * Sends word counts to {@link JsonWriter#writeObject(Map, Path)} to write to {@code path}.
	 * @param path The output file path to write to
	 * @throws IOException if an IO error occurs
	 */
	public static void countFlag(Path path, InvertedIndex invertedIndex) throws IOException {
		JsonWriter.writeObject(invertedIndex.wordStems, path);
	}

	/**
	 * Sends inverted index to {@link JsonWriter#writeObjectObject(Map, Path)} to write to {@code path}.
	 * @param path The output file path to write to
	 * @throws IOException If an IO error occurs
	 */
	public static void indexFlag(Path path, InvertedIndex invertedIndex) throws IOException {
		JsonWriter.writeObjectObject(invertedIndex.invertedIndex, path);
	}
}

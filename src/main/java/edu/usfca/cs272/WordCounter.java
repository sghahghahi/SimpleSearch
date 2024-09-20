package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/*
 * TODO Move the IO stuff into FileIndexer (or similar name)
 */

/**
 * Class responsible for for calculating word counts and building an inverted index.
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class WordCounter { // TODO Refactor InvertedIndex

	/** {@code TreeMap} to store file path and word count key/value pairs */
	private final TreeMap<String, Integer> wordStems;

	/** Stores words with their file paths and word positions */
	private final TreeMap<String, Map<String, Collection<? extends Number>>> invertedIndex;
	// TODO private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/** Keeps track of the word position in each file */
	private int wordPosition = 1;

	/**
	 * Default constructor that initializes a new word counter and inverted index.
	 */
	public WordCounter() {
		this.wordStems = new TreeMap<>();
		this.invertedIndex = new TreeMap<>();
	}

	/**
	 * Reads the given line and adds the path and number of word stems
	 * as a key/value pair to {@code this.wordStems}.
	 * Builds an inverted index if the {@code -index} flag is present.
	 * @param line The line to read
	 * @param path Where the file to be read is
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	private void calculateWordCount(String line, Path path) throws IOException {
		buildInvertedIndex(line, path);

		// Add file path and word count to TreeMap
		this.wordStems.put(
			path.toString(),
			this.wordStems.getOrDefault(path.toString(), 0) + FileStemmer.listStems(line).size()
		);
	}

	/**
	 * Adds each word in the line, its file path, and its word position in the file to an inverted index.
	 * @param line The line to read
	 * @param path Where the file to be read is
	 */
	private void buildInvertedIndex(String line, Path path) {
		ArrayList<String> words = FileStemmer.listStems(line);
		for (String word : words) {
			word = word.toLowerCase();

			var innerMap = this.invertedIndex.get(word); // TODO This is the start of the add method
			if (innerMap == null) {
				innerMap = new TreeMap<>();
			}

			var innerCollection = innerMap.get(path.toString());
			ArrayList<Number> innerList = innerCollection == null ? new ArrayList<>() : new ArrayList<>(innerCollection);

			innerList.add(this.wordPosition++);
			innerMap.put(path.toString(), innerList);

			this.invertedIndex.put(word, innerMap);
		}
	}

	/**
	 * Reads file from {@code path}.
	 * Adds {@code path} and word counts to {@code this.wordStems} to be written to a file later.
	 * @param path File path to read from
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	private void readFile(Path path) throws IOException { // TODO public
		// TODO Create a stemmer here and as you stem here
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				// TODO add(stem, path.toString(), counter++)
				calculateWordCount(line, path);
			}

			// TODO the counter is the word count
		}
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirPath}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirPath path of directory to traverse
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	private void readDir(Path dirPath) throws IOException { // TODO public
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
			for (Path path : dirStream) {
				if (Files.isDirectory(path)) {
					readDir(path);
				} else {
					if (isTextFile(path)) {
						// Reset word position to 1 every time we read from a new file
						this.wordPosition = 1;
						readFile(path);
					}
				}
			}
		}
	}

	/**
	 * @param path The file path to check
	 * @return If the file at {@code path} ends with {@code .txt} or {@code .text} (case-insensitive).
	 */
	private static boolean isTextFile(Path path) { // TODO public
		String lowerCasePath = path.toString().toLowerCase();

		return lowerCasePath.endsWith(".txt") ||
		lowerCasePath.endsWith(".text");
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code path} to its appropriate method.
	 * @param path The path of either a directory or file
	 * @param indexFlag Whether the {@code -index} flag is present
	 * @throws IOException If an IO error occurs
	 */
	public void textFlag(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			readDir(path);
		} else {
			readFile(path);
		}
	}

	/**
	 * Sends word counts to {@link JsonWriter#writeObjectObject(Map, Path)} to write to {@code path}.
	 * @param path The output file path to write to
	 * @throws IOException if an IO error occurs
	 */
	public void countFlag(Path path) throws IOException {
		JsonWriter.writeObject(this.wordStems, path);
	}

	/**
	 * Sends inverted index to {@link JsonWriter#writeObjectObject(Map, Path)} to write to {@code path}.
	 * @param path The output file path to write to
	 * @throws IOException If an IO error occurs
	 */
	public void indexFlag(Path path) throws IOException {
		JsonWriter.writeObjectObject(this.invertedIndex, path);
	}
}

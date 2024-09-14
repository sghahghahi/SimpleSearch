package edu.usfca.cs272;

import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import java.io.BufferedReader;
import java.io.IOException;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for for calculating word counts and building an inverted index.
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class WordCounter {

	/** {@code TreeMap} to store file path and word count key/value pairs */
	private final TreeMap<String, Integer> wordStems;

	/** Stores words with their file paths and word positions */
	private final TreeMap<String, Map<String, Collection<? extends Number>>> invertedIndex;

	/** Keeps track of the word position in each file */
	private int wordPosition = 1;

	/**
	 * Default constructor that initializes a new word counter and inverted index
	 */
	public WordCounter() {
		this.wordStems = new TreeMap<>();
		this.invertedIndex = new TreeMap<>();
	}

	/**
	 * Reads the given line and adds the path and number of word stems
	 * as a key/value pair to {@code this.wordStems}
	 * @param line The line to read
	 * @param snowballStemmer The stemmer to use
	 * @param path Where the file to be read is
	 */
	private void calculateWordCount(String line, Path path, boolean indexFlag) throws IOException {
		if (indexFlag == true) {
			buildInvertedIndex(line, path);
		}

		// Add file path and word count to TreeMap
		this.wordStems.put(
			path.toString(),
			this.wordStems.getOrDefault(path.toString(), 0) + FileStemmer.listStems(line).size()
		);
	}

	/**
	 * Adds each word in the line, its file path, and its word position in the file to an inverted index
	 * @param line The line to read
	 * @param path Where the file to be read is
	 */
	private void buildInvertedIndex(String line, Path path) {
		ArrayList<String> words = FileStemmer.listStems(line);
		for (String word : words) {
			word = word.toLowerCase();

			var innerMap = this.invertedIndex.get(word);
			if (innerMap == null) {
				innerMap = new TreeMap<>();
			}

			var innerCollection = innerMap.get(path.toString());
			ArrayList<Number> innerList = (innerCollection == null) ? new ArrayList<>() : new ArrayList<>(innerCollection);

			innerList.add(this.wordPosition);
			innerMap.put(path.toString(), innerList);
			this.invertedIndex.put(word, innerMap);

			this.wordPosition++;
		}
	}

	/**
	 * Reads file from {@code path} and adds word stems from file to an {@code ArrayList}.
	 * Adds {@code path} and word counts to {@code this.TreeMap} to be written to a file later.
	 * @param path File path to read from
	 */
	private void readFile(Path path, boolean indexFlag)
		throws IOException,
		NullPointerException,
		UnsupportedOperationException,
		ClassCastException,
		IllegalArgumentException,
		IllegalStateException
	{
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line = null;

			while ((line = reader.readLine()) != null) {
				calculateWordCount(line, path, indexFlag);
			}
		}
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirPath}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirPath path of directory to traverse
	 */
	private void readDir(Path dirPath, boolean indexFlag) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
			for (Path path : dirStream) {
				if (Files.isDirectory(path)) {
					readDir(path, indexFlag);
				} else {
					if (isTextFile(path)) {
						// Reset word position to 1 every time we read from a new file
						this.wordPosition = 1;
						readFile(path, indexFlag);
					}
				}
			}
		}
	}

	/**
	 * @param path The file path to check
	 * @return If the file at {@code path} ends with {@code .txt} or {@code .text} (case-insensitive).
	 */
	private static boolean isTextFile(Path path) {
		String lowerCasePath = path.toString().toLowerCase();

		return (
			lowerCasePath.endsWith(".txt") ||
			lowerCasePath.endsWith(".text")
		);
	}

	/**
	 * Writes {@code this.TreeMap} to the output file at {@code path} in pretty JSON format.
	 * @param path File path to write to
	 * @param indexFlag Whether the {@code -index} flag is present
	 */
	private void writeFile(Path path, boolean indexFlag) throws IOException {
		if (indexFlag == true) {
			JsonWriter.writeObjectObject(this.invertedIndex, path);
		} else {
			JsonWriter.writeObject(this.wordStems, path);
		}
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code path} to its appropriate method.
	 * @param path The path of either a directory or file
	 */
	public void textFlag(Path path, boolean indexFlag) throws IOException {
		if (Files.isDirectory(path)) {
			readDir(path, indexFlag);
		} else {
			readFile(path, indexFlag);
		}
	}

	/**
	 * Sends {@code path} to {@link #writeFile(Path)} to be processed and output.
	 * @param path The output file path to write to
	 */
	public void countFlag(Path path, boolean indexFlag) throws IOException {
		writeFile(path, indexFlag);
	}

	public void indexFlag(Path path, boolean indexFlag) throws IOException {
		writeFile(path, indexFlag);
	}
}

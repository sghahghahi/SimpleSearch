package edu.usfca.cs272;

import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.TreeMap;

import java.io.BufferedReader;
import java.io.IOException;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class WordCounter {

	/** {@code TreeMap} to store file path and word count key/value pairs */
	private final TreeMap<String, Integer> invertedIndex;

	public WordCounter() {
		this.invertedIndex= new TreeMap<>();
	}

	private void calculateWordCount(String line, SnowballStemmer snowballStemmer, Path path) {
		ArrayList<String> wordStems = new ArrayList<>();

		// Populate ArrayList with word stems
		FileStemmer.addStems(line, snowballStemmer, wordStems);

		// Add file path and word count to TreeMap
		this.invertedIndex.put(
			path.toString(),
			this.invertedIndex.getOrDefault(path.toString(), 0) + wordStems.size()
		);
	}

	/**
	 * Reads file from {@code path} and adds word stems from file to an {@code ArrayList}.
	 * Adds {@code path} and word counts to {@code this.TreeMap} to be written to a file later.
	 * @param path File path to read from
	 */
	private void readFile(Path path)
		throws IOException,
		NullPointerException,
		UnsupportedOperationException,
		ClassCastException,
		IllegalArgumentException,
		IllegalStateException
	{
		try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
			String line = null;
			SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);

			while ((line = reader.readLine()) != null) {
				calculateWordCount(line, snowballStemmer, path);
			}
		}
	}

	/**
	 * Recursively reads all files and subdirectories from {@code dirPath}.
	 * Only reads files if they end in {@code .txt} or {@code .text} (case-insensitive).
	 * @param dirPath path of directory to traverse
	 */
	private void readDir(Path dirPath) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
			for (Path path : dirStream) {
				if (Files.isDirectory(path)) {
					readDir(path);
				} else {
					if (isTextFile(path)) {
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
	 */
	private void writeFile(Path path) throws IOException {
		JsonWriter.writeObject(this.invertedIndex, path);
	}

	/**
	 * Reads {@code path}.
	 * Sends the directory or file at {@code path} to its appropriate method.
	 * @param path The path of either a directory or file
	 */
	public void textFlag(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			readDir(path);
		} else {
			readFile(path);
		}
	}

	/**
	 * Sends {@code path} to {@link #writeFile(Path)} to be processed and output.
	 * @param path The output file path to write to
	 */
	public void countFlag(Path path) throws IOException {
		writeFile(path);
	}
}
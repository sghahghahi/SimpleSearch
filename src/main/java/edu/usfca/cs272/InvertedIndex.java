package edu.usfca.cs272;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for for calculating word counts and building an inverted index.
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class InvertedIndex {

	/** {@code TreeMap} to store file path and word count key/value pairs */
	private final TreeMap<String, Integer> wordStems;

	/** Stores words with their file paths and word positions */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * Default constructor that initializes a new word counter and inverted index.
	 */
	public InvertedIndex() {
		this.wordStems = new TreeMap<>();
		this.invertedIndex = new TreeMap<>();
	}

	/**
	 * Adds the {@code location} and number of stems found in it to
	 * our {@code TreeMap} that stores them as key/value pairs.
	 * @param location - Where the stems are found
	 * @param count - The number of stems
	 * @return {@code true} if the add was successful
	 */
	public boolean addCount(String location, int count) {
		if (count == 0) {
			return false;
		}

		// this.wordStems.put(location, count);
		this.wordStems.put(
			location,
			this.wordStems.getOrDefault(location, 0) + count
		);

		return true;
	}

	/**
	 * Adds a {@code List} of all stemmed words at a specific {@code location} to an inverted index
	 * @param stemmedWords - The {@code List} of <strong>stemmed</strong> words to add
	 * @param location - Where the stemmed word was found
	 * @param wordPosition - The word position of the {@code cleanedWord} in the file
	 * @return {@code true} if the add was successfull
	 */
	public boolean addWords(List<String> stemmedWords, String location, int wordPosition) {
		for (String stemmedWord : stemmedWords) {
			addWordPosition(stemmedWord, location, wordPosition++);
		}

		return true;
	}
	
	/*
	 * TODO Change path to location (document, source)
	 */

	/**
	 * Adds the {@code word} and its {@code location} to the inverted index.
	 * @param word - The word to add
	 * @param location - Where the word was found
	 * @param wordPosition - The position of the {@code word} in the file
	 * @return {@code true} if the add was successful
	 */
	public boolean addWordPosition(String word, String location, int wordPosition) {
		var innerMap = this.invertedIndex.get(word);
		if (innerMap == null) {
			innerMap = new TreeMap<>();
			this.invertedIndex.put(word, innerMap);
		}

		TreeSet<Integer> innerList = innerMap.get(location);

		if (innerList == null) {
			innerList = new TreeSet<>();
			innerMap.put(location, innerList);
		}

		return innerList.add(wordPosition);
	}

	/**
	 * Returns a view of the location/word count key/value pair map
	 * @return An unmodifiable view of the location/word count key/value pair map
	 */
	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(this.wordStems);
	}

	/**
	 * Returns the number of key/value pairs in the {@code TreeMap}
	 * storing file paths and the number of stems at each path
	 * @return The number of key/value pairs
	 */
	public int countSize() {
		return this.wordStems.size();
	}

	/**
	 * Returns the number of stems found at {@code path}
	 * @param path - Where the stems were found
	 * @return The number of stems found at {@code path}
	 * or {@code -1} if {@code path} is not in the {@code TreeMap}
	 */
	public int numStems(String path) {
		return this.wordStems.getOrDefault(path, -1);
	}

	/**
	 * Returns {@code true} if this {@code TreeMap} does not contain
	 * any key/value pairs
	 * @return {@code true} if this {@code TreeMap} contains no key/value pairs
	 */
	public boolean isEmpty() {
		return this.wordStems.isEmpty();
	}

	/**
	 * Returns {@code true} if {@code path} is in the {@code TreeMap}
	 * storing file paths and the number of stems at each path
	 * @param path - The path to look up in the {@code TreeMap}
	 * @return {@code true} if {@code path} is in the {@code TreeMap}
	 */
	public boolean containsPath(String path) {
		return this.wordStems.containsKey(path);
	}
	
	/* TODO 
	public void indexJson(Path path) throws IOException {
		JsonWriter.writeObjectObject(this.invertedIndex, path);
	}
	*/

	/**
	 * Returns a view of the inverted index
	 * @return An unmodifiable view of the inverted index
	 */
	public Map<String, TreeMap<String, TreeSet<Integer>>> getIndex() { // TODO Remove
		return Collections.unmodifiableMap(this.invertedIndex);
	}

	/**
	 * Returns a {@code TreeMap} containing file paths where {@code word} was found and
	 * how many times it occured in the file
	 * @param word - The word to find in the inverted index
	 * @return An unmodifiable {@code TreeMap} containing file paths and word counts
	 * for the specified {@code word} or {@code null} if {@code word} is not
	 * in the inverted index
	 */
	public Map<String, TreeSet<Integer>> getPaths(String word) { // TODO Remove
		return Collections.unmodifiableMap(this.invertedIndex.get(word));
	}
	
	/* TODO 
	getWords --> unmodifiable view of the invertedIndex.keySet
	getLocations --> given a word, returns the keyset of the innermap
	getPositions --> given a word and location, returns the inner most positions
	
	make more size and contains methods for each levle of nesting in the inverted index
	*/

	/**
	 * Returns the number of key/value pairs in the inverted index
	 * @return The number of words in the inverted index
	 */
	public int indexSize() { // TODO numWords
		return this.invertedIndex.size();
	}

	/**
	 * Returns {@code true} if {@wcode word} is in the inverted index
	 * @param word - The word to look up in the inverted index
	 * @return {@code true} if {@code word} is in the inverted index
	 */
	public boolean containsWord(String word) {
		return this.invertedIndex.containsKey(word);
	}

	/**
	 * String representation of the inverted index that outputs
	 * the current amount of words stored in it
	 */
	@Override
	public String toString() {
		return String.format(
			"Inverted index currently has %d words stored.",
			indexSize()
		);
	}
}

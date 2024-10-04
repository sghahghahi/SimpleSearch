package edu.usfca.cs272;

import java.io.IOException;

import java.nio.file.Path;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	/** {@code TreeMap} to store file location and word count key/value pairs */
	private final TreeMap<String, Integer> wordStems;

	/** Stores words with their file locations and word positions */
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
	 * @return {@code true} if the add was successful
	 */
	public boolean addWords(List<String> stemmedWords, String location, int wordPosition) {
		for (String stemmedWord : stemmedWords) {
			addWordPosition(stemmedWord, location, wordPosition++);
		}

		return true;
	}

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
	 * storing file locations and the number of stems at each location
	 * @return The number of key/value pairs
	 */
	public int numCounts() {
		return this.wordStems.size();
	}

	/**
	 * Returns the number of stems found at {@code location}
	 * @param location - Where the stems were found
	 * @return The number of stems found at {@code location}
	 * or {@code -1} if {@code location} is not in the {@code TreeMap}
	 */
	public int numStems(String location) {
		return this.wordStems.getOrDefault(location, -1);
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
	 * Returns {@code true} if {@code location} is in the {@code TreeMap}
	 * storing file locations and the number of stems at each location
	 * @param location - The location to look up in the {@code TreeMap}
	 * @return {@code true} if {@code location} is in the {@code TreeMap}
	 */
	public boolean containsLocation(String location) {
		if (location == null) {
			return false;
		}

		return this.wordStems.containsKey(location);
	}

	public void indexCounts(Path location) throws IOException {
		JsonWriter.writeObject(this.wordStems, location);
	}

	/* Methods for inverted index */

	public void indexJson(Path location) throws IOException {
		JsonWriter.writeObjectObject(this.invertedIndex, location);
	}

	/**
	 * Returns the number of locations where {@code word} was found
	 * @param word - The word to look up locations for
	 * @return The number of locations where {@code word} was found
	 * or {@code -1} if {@code word} is not in the inverted index
	 */
	public int numLocations(String word) {
		if (word == null || !containsWord(word)) {
			return -1;
		}

		return this.invertedIndex.get(word).size();
	}

	/**
	 * Returns the number of word positions that {@code word} was found in {@code location}
	 * @param word - The word to look up in the inverted index
	 * @param location - The location associated with {@code word} in the inverted index
	 * @return The number of word positions that {@code word} was found in {@code location}
	 * or {@code -1} if either {@code word} or {@code location} are not in the inverted index
	 */
	public int numPositions(String word, String location) {
		if (
			word == null ||
			location == null ||
			!containsWord(word) ||
			!containsLocation(location)
		) {
			return -1;
		}

		return this.invertedIndex.get(word).get(location).size();
	}

	/**
	 * Returns a view of the words stored in our word counts data structure
	 * @return An unmodifiable view of the kes in the word counts data structure
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(this.wordStems.keySet());
	}

	/**
	 * Returns the number of key/value pairs in the inverted index
	 * @return The number of words in the inverted index
	 */
	public int numWords() {
		return this.invertedIndex.size();
	}

	/**
	 * Returns {@code true} if {@wcode word} is in the inverted index
	 * @param word - The word to look up in the inverted index
	 * @return {@code true} if {@code word} is in the inverted index
	 */
	public boolean containsWord(String word) {
		if (word == null) {
			return false;
		}

		return this.invertedIndex.containsKey(word);
	}

	/**
	 * Returns a {@code Set} of locations mapped to a specific {@code word}
	 * @param word - The word to look up in the inverted index
	 * @return An unmodifiable view of the locations mapped to {@code word} or
	 * {@code null} if {@code word} is not in the inverted index
	 */
	public Set<String> getLocations(String word) {
		if (
			word == null || !containsWord(word)) {
			return null;
		}

		return Collections.unmodifiableSet(this.invertedIndex.get(word).keySet());
	}

	/**
	 * Returns the positions of {@code word} at {@code location}
	 * @param word - The word to look up in the inverted index
	 * @param location - Where {@code word} was found
	 * @return An unmodifiable view of the word positions of {@code word} found at {@code location} or
	 * {@code null} if either {@code word} or {@code location} is not in the inverted index
	 */
	public Set<Integer> getPositions(String word, String location) {
		if (
			word == null ||
			location == null ||
			!containsWord(word) ||
			!containsLocation(location)
		) {
			return null;
		}

		return Collections.unmodifiableSet(this.invertedIndex.get(word).get(location));
	}

	/**
	 * String representation of the inverted index that outputs
	 * the current amount of words stored in it
	 */
	@Override
	public String toString() {
		int size = numWords();
		return String.format(
			"Inverted index currently has %d %s stored.",
			size,
			size == 1 ? "word" : "words"
		);
	}
}

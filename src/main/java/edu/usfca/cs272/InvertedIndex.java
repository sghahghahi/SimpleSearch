package edu.usfca.cs272;

import java.io.IOException;

import java.nio.file.Path;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

	/** Class that represents a search result */
	public class SearchResult implements Comparable<SearchResult> {
		protected int count;
		protected double score;
		protected String location;

		/**
		 * Constructor that takes in a count, score, and location to be stored as part of a search result
		 * @param count
		 * @param score
		 * @param location
		 */
		public SearchResult(int count, double score, String location) {
			this.count = count;
			this.score = score;
			this.location = location;
		}

		/**
		 * Default constructor that sets all fields to {@code 0}
		 */
		public SearchResult() {
			this(0, 0, "");
		}

		@Override
		public int compareTo(SearchResult o) {
			int result = Double.compare(o.score, this.score);
			if (result != 0) {
				return result;
			}

			result = Integer.compare(o.count, this.count);
			if (result != 0) {
				return result;
			}

			return this.location.compareTo(o.location);
		}
	}

	/**
	 * Performs an exact search of {@code queryStems} on the inverted index
	 * @param queryStems - The query stems to search
	 * @return A sorted {@code List} of {@code SearchResult} objects
	 */
	public List<SearchResult> exactSearch(Set<String> queryStems) {
		// Stores a location and a search result for that location
		HashMap<String, SearchResult> lookup = new HashMap<>();

		for (String queryStem : queryStems) {
			if (containsWord(queryStem)) {
				for (String location : getLocations(queryStem)) {
					int matches = numPositions(queryStem, location);

					SearchResult existingResult = lookup.get(location);
					if (existingResult == null) {
						existingResult = new SearchResult(0, 0, location);
						lookup.put(location, existingResult);
					}

					existingResult.count += matches;
				}
			}
		}

		for (SearchResult result : lookup.values()) {
			int numStems = numStems(result.location);
			result.score = calculateScore(result.count, numStems);
		}

		List<SearchResult> searchResults = new ArrayList<>(lookup.values());
		Collections.sort(searchResults);

		return searchResults;
	}

	/**
	 * Performs a partial search of {@code queryStems} on the inverted index
	 * @param queryStems - The query stems to search
	 * @return A sorted {@code List} of {@code SearchResult} objects
	 */
	public List<SearchResult> partialSearch(Set<String> queryStems) {
		// Stores a location and a search result for that location
		HashMap<String, SearchResult> lookup = new HashMap<>();

		for (String queryStem : queryStems) {
			for (String word : getWords()) {
				if (word.startsWith(queryStem)) {
					for (String location : getLocations(word)) {
						int matches = numPositions(word, location);

						SearchResult existingResult = lookup.get(location);
						if (existingResult == null) {
							existingResult = new SearchResult(0, 0, location);
							lookup.put(location, existingResult);
						}

						existingResult.count += matches;
					}
				}
			}
		}

		for (SearchResult result : lookup.values()) {
			int numStems = numStems(result.location);
			result.score = calculateScore(result.count, numStems);
		}

		List<SearchResult> searchResults = new ArrayList<>(lookup.values());
		Collections.sort(searchResults);

		return searchResults;
	}

	/**
	 * Calculates the score for a search result
	 * @param matches - Number of matches for the current result
	 * @param wordCount - Number of stems
	 * @return The score of the search result based on this calculation: {@code matches / wordCount}
	 */
	private double calculateScore(int matches, int wordCount) {
		return (double) matches / wordCount;
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
	public int addWords(List<String> stemmedWords, String location, int wordPosition) {
		for (String stemmedWord : stemmedWords) {
			addWordPosition(stemmedWord, location, wordPosition++);
		}

		return wordPosition - 1;
	}

	/**
	 * Adds the {@code word} and its {@code location} to the inverted index.
	 * @param word - The word to add
	 * @param location - Where the word was found
	 * @param wordPosition - The position of the {@code word} in the file
	 * @return {@code true} if the add was successful
	 */
	public boolean addWordPosition(String word, String location, int wordPosition) {
		var locations = this.invertedIndex.get(word);
		if (locations == null) {
			locations = new TreeMap<>();
			this.invertedIndex.put(word, locations);
		}

		TreeSet<Integer> positions = locations.get(location);

		if (positions == null) {
			positions = new TreeSet<>();
			locations.put(location, positions);
		}

		return positions.add(wordPosition);
	}

	/**
	 * Returns a view of the location/word count key/value pair map
	 * @return An unmodifiable view of the location/word count key/value pair map
	 */
	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(this.wordStems);
	}

	/**
	 * Returns a view of the words stored in our word counts data structure
	 * @return An unmodifiable view of the keys in the word counts data structure
	 */
	public Set<String> getStemLocations() {
		return Collections.unmodifiableSet(this.wordStems.keySet());
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
	 */
	public int numStems(String location) {
		return this.wordStems.getOrDefault(location, 0);
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
		return this.wordStems.containsKey(location);
	}

	/**
	 * Writes the word stems data structure as a pretty JSON object
	 * @param location - Where to write the word stems data structure to
	 * @throws IOException If an IO error occurs
	 */
	public void indexCounts(Path location) throws IOException {
		JsonWriter.writeObject(this.wordStems, location);
	}

	/**
	 * Writes the inverted index as a pretty JSON object
	 * @param location - Where to write the inverted index to
	 * @throws IOException If an IO error occurs
	 */
	public void indexJson(Path location) throws IOException {
		JsonWriter.writeObjectObject(this.invertedIndex, location);
	}

	/**
	 * Returns the number of locations where {@code word} was found
	 * @param word - The word to look up locations for
	 * @return The number of locations where {@code word} was found
	 */
	public int numLocations(String word) {
		var locations = this.invertedIndex.get(word);
		return locations == null ? 0 : locations.size();
	}

	/**
	 * Returns the number of word positions that {@code word} was found in {@code location}
	 * @param word - The word to look up in the inverted index
	 * @param location - The location associated with {@code word} in the inverted index
	 * @return The number of word positions that {@code word} was found in {@code location}
	 */
	public int numPositions(String word, String location) {
		return getPositions(word, location).size();
	}

	/**
	 * Returns the number of key/value pairs in the inverted index
	 * @return The number of words in the inverted index
	 */
	public int numWords() {
		return this.invertedIndex.size();
	}

	/**
	 * Returns a {@code Set} of locations mapped to a specific {@code word}
	 * @param word - The word to look up in the inverted index
	 * @return An unmodifiable view of the locations mapped to {@code word} or
	 * an empty {@code Set} if {@code word} is not in the inverted index
	 */
	public Set<String> getLocations(String word) {
		if (word == null || !containsWord(word)) {
			return Collections.emptySet();
		}

		return Collections.unmodifiableSet(this.invertedIndex.get(word).keySet());
	}

	/**
	 * Returns the positions of {@code word} at {@code location}
	 * @param word - The word to look up in the inverted index
	 * @param location - Where {@code word} was found
	 * @return An unmodifiable view of the word positions of {@code word} found at {@code location} or
	 * an empty {@code Set} if {@code word} or {@code location} are not in the inverted index
	 */
	public Set<Integer> getPositions(String word, String location) {
		var locations = this.invertedIndex.get(word);
		if (locations == null) {
			return Collections.emptySet();
		}

		var wordPositions = locations.get(location);
		if (wordPositions == null) {
			return Collections.emptySet();
		}

		return Collections.unmodifiableSet(wordPositions);
	}

	/**
	 * Returns an unmodifiable view of the words in the inverted index
	 * @return An unmodifiable view of the words (keys) in the inverted index
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(this.invertedIndex.keySet());
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
	 * Checks the inverted index if {@code location} at {@code word} exists
	 * @param word - The word to look up in the inverted index
	 * @param location - The location corresponding to {@code word}
	 * @return {@code true} if {@code location} is in the inverted index
	 */
	public boolean containsLocation(String word, String location) {
		var wordLocations = this.invertedIndex.get(word);
		if (wordLocations == null) {
			return false;
		}

		return wordLocations.containsKey(location);
	}

	/**
	 * Checks the inverted index for a specific position where {@code word} was found at {@code location}
	 * @param word - The word to look up in the inverted index
	 * @param location - The location associated with {@code word}
	 * @param position - The position to check within {@code location} for {@code word} in the inverted index
	 * @return {@code true} if {@code position} was found in the inverted index at {@code location} for {@code word}
	 */
	public boolean containsPosition(String word, String location, int position) {
		var locations = this.invertedIndex.get(word);
		if (locations == null) {
			return false;
		}

		var wordPositions = locations.get(location);
		if (wordPositions == null) {
			return false;
		}

		return wordPositions.contains(position);
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

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
		for (String stemmedWord: stemmedWords) {
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
			innerMap.put(location.toString(), innerList);
		}

		return innerList.add(wordPosition);
	}

	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(this.wordStems);
	}

	public Map<String, TreeMap<String, TreeSet<Integer>>> getIndex() {
		return Collections.unmodifiableMap(this.invertedIndex);
	}


	/*
	 * Start adding other generally useful methods
	 *
	 * Think about each data structure
	 * And has/contains methods, num/size methods, get/view methods
	 * toString
	 */}

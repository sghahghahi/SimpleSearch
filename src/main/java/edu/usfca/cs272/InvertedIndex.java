package edu.usfca.cs272;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.nio.file.Path;

import java.util.ArrayList;
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
	protected final TreeMap<String, Integer> wordStems;

	/** Stores words with their file paths and word positions */
	protected final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/** Keeps track of the word position in each file. Stored in inverted index. */
	protected int wordPosition = 1;  // TODO Remove

	/**
	 * Default constructor that initializes a new word counter and inverted index.
	 */
	public InvertedIndex() {
		this.wordStems = new TreeMap<>();
		this.invertedIndex = new TreeMap<>();
	}

	/**
	 * Reads the given line and adds the path and number of word stems
	 * as a key/value pair to the word stems {@code Map}
	 * Builds an inverted index if the {@code -index} flag is present.
	 * @param line The line to read
	 * @param snowballStemmer The stemmer to use
	 * @param path Where the file to be read is
	 * @return {@code true} if the add was successful
	 */
	// TODO addCount(String location, int count) (don't put if not > 0)
	public boolean addWordCounts(String line, SnowballStemmer snowballStemmer, Path path) {
		this.wordStems.put(
			path.toString(),
			this.wordStems.getOrDefault(path.toString(), 0) + FileStemmer.listStems(line, snowballStemmer).size()
		);

		return true;
	}

	/**
	 * Adds each word in the line, its file path, and its word position in the file to an inverted index.
	 * @param line The line to read
	 * @param snowballStemmer The stemmer to use
	 * @param path Where the file to be read is
	 */
	public void buildInvertedIndex(String line, SnowballStemmer snowballStemmer, Path path) {
		ArrayList<String> words = FileStemmer.listStems(line, snowballStemmer); // TODO Move to IOHandler...
		for (String word : words) {
			// addWordPosition(word.toLowerCase(), path.toString());
		}
	}

	/* TODO CHange to this instead:
	public boolean addWords(List<String> words, String location) {
		for (String word : words) {
			addWordPosition(...)
		}
	}
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
			innerMap.put(location.toString(), innerList);
		}

		return innerList.add(wordPosition);
	}

	/*
	 * Start adding other generally useful methods
	 *
	 * Think about each data structure
	 * And has/contains methods, num/size methods, get/view methods
	 * toString
	 */}

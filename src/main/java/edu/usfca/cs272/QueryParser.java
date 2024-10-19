package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class QueryParser {
	/** {@code TreeMap} that maps each query string to a {@code Map} of locations and its {@code SearchResult} objects */
	private final TreeMap<String, Map<String, SearchResult>> searchResults;

	/** Initialized and populated inverted index object to reference */
	private final InvertedIndex invertedIndex;

	/** Flag to specify whether an exact search or partial search should be executed. Defaults to exact search */
	private boolean exactSearch;

	/** Stemmer to use file-wide */
	private final SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);

	/** Class that represents a search result */
	class SearchResult { // TODO public and non-static, but inside of inverted index instead, and implement comparable
		int count;
		double score;
		String location;

		public SearchResult(int count, double score, String location) {
			this.count = count;
			this.score = score;
			this.location = location;
		}

		public SearchResult() {
			this(0, 0, null);
		}

		@Override
		public String toString() {
			return String.format(
				"This search was conducted at location: %s",
				this.location
			);
		}
	}

	/**
	 * Constrcutor that initializes our search result metadata data structure to an empty {@code TreeMap}
	 * @param invertedIndex - The inverted index object to reference. We are not constructing a new inverted index in this class.
	 * This inverted index is passed from the caller and is assumed to be properly initialized and populated
	 */
	public QueryParser(InvertedIndex invertedIndex /* boolean exactSearch */ ) {
		this.searchResults= new TreeMap<>();
		this.invertedIndex = invertedIndex;
		this.exactSearch = true;
	}

	/*
	 * TODO Don't need directory traversal for query file parsing, for now until after
	 * multithreading, don't worry about supporting this (just process a single file at a time)
	 */
	
	/**
	 * Checks to see if {@code queryLocation} is a directory or a file. Handles file accordingly
	 * @param queryLocation - The path to the query file
	 * @param lookupLocation - The path to the file to build the inverted index from
	 * @throws IOException If an IO error occurs
	 */
	public void checkLocation(Path queryLocation, Path lookupLocation) throws IOException {
		if (Files.isDirectory(lookupLocation)) {
			queryDirectory(queryLocation, lookupLocation);
		} else {
			queryLocation(queryLocation, lookupLocation);
		}
	}

	/**
	 * Recursively traverses {@code queryLocation} and processes each file ending in either {@code .txt} or {@code .text}
	 * @param queryLocation - The path to the query file
	 * @param lookupLocation - The path to the file where the inverted index is built
	 * @throws IOException If an IO error occurs
	 */
	private void queryDirectory(Path queryLocation, Path lookupLocation) throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(lookupLocation)) {
			for (Path location : dirStream) {
				if (Files.isDirectory(location)) {
					queryDirectory(queryLocation, location);
				} else if (TextFileIndexer.isTextFile(location)) {
					queryLocation(queryLocation, location);
				}
			}
		}
	}

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation - Where to find the query words
	 * @param lookupLocation - Where the words associated with the query stems are located
	 * @throws IOException - If an IO error occurs
	 */
	private void queryLocation(Path queryLocation, Path lookupLocation) throws IOException {
		TreeSet<String> queryStems;

		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				queryStems = FileStemmer.uniqueStems(line, snowballStemmer);

				if (this.exactSearch) {
					exactSearch(queryStems, lookupLocation);
				} else {
					partialSearch(queryStems, lookupLocation);
				}
			}
		}
	}

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems - The query stems to Stringify
	 * @return The space-separated query {@code String}
	 */
	private String extractQueryString(Set<String> queryStems) {
		// TODO String.join(" ", queryStems)
		StringBuilder queryString = new StringBuilder();

		for (String queryStem : queryStems) {
			queryString.append(queryStem + " ");
		}

		return queryString.toString().strip();
	}

	/**
	 * Performs an exact search of {@code queryStems} on the inverted index
	 * @param queryStems - The query stems to search
	 * @param location - Where the words associated with {@code queryStems} are located
	 */
	private void exactSearch(Set<String> queryStems, Path location) {
		if (queryStems.isEmpty()) {
			return;
		}

		int wordCount = this.invertedIndex.numStems(location.toString());
		int matches = 0;

		for (String queryStem : queryStems) {
			int numPositions = this.invertedIndex.numPositions(queryStem, location.toString());
			matches += numPositions;
		}

		addSearchResult(queryStems, location.toString(), matches, wordCount);
	}

	/**
	 * Performs a partial search of {@code queryStems} on the inverted index
	 * @param queryStems - The query stems to search
	 * @param location - Where the words associated with {@code queryStems} are located
	 */
	private void partialSearch(Set<String> queryStems, Path location) {
		if (queryStems.isEmpty()) {
			return;
		}

		int wordCount = this.invertedIndex.numStems(location.toString());
		int matches = 0;

		for (String queryStem : queryStems) {
			for (String word : this.invertedIndex.getWords()) {
				if (word.startsWith(queryStem)) {
					int numPositions = this.invertedIndex.numPositions(word, location.toString());
					matches += numPositions;
				}

			}
		}

		addSearchResult(queryStems, location.toString(), matches, wordCount);
	}

	/**
	 * Adds a {@code SearchResult} object to the {@code Map} of {@code SearchResult} objects
	 * @param queryStems - The stems from the query file
	 * @param location - The file location where matches from each stem in {@code queryStems} was found
	 * @param matches - The number of matches from a stem in {@code wordStems} to a word in the inverted index
	 * @param wordCount - The total number of words found at {@code location}
	 */
	private void addSearchResult(Set<String> queryStems, String location, int matches, int wordCount) {
		double score = calculateScore(matches, wordCount);
		String queryString = extractQueryString(queryStems);

		Map<String, SearchResult> queryResults = this.searchResults.get(queryString);
		if (queryResults == null) {
			queryResults = new HashMap<>();
			this.searchResults.put(queryString, queryResults);
		}

		if (matches != 0 && score != 0) {
			SearchResult existingResult = queryResults.get(location);

			if (existingResult == null) {
				SearchResult searchResult = new SearchResult(matches, score, "\"" + location + "\"");
				queryResults.put(location, searchResult);

			} else if (score > existingResult.score || (score == existingResult.score && matches > existingResult.count)) {
					existingResult.count = matches;
					existingResult.score = score;
			}
		}
	}

	/**
	 * Calculates the score for a search result
	 * @param matches - Number of matches for the current result
	 * @param wordCount - Number of stems
	 * @return The score of the search result based on this calculation: {@code matches / wordcount}
	 */
	private double calculateScore(int matches, int wordCount) {
		return (double) matches / wordCount;
	}

	/**
	 * Sorts the {@code List} of {@code SearchResult} objects based on their
	 *   score, count, and location using a lambda function
	 */
	private void sortSearchResults(List<SearchResult> searchResults) {
		searchResults.sort((o1, o2) -> {
			int result = Double.compare(o2.score, o1.score);
			if (result != 0) {
				return result;
			}

			result = Integer.compare(o2.count, o1.count);
			if (result != 0) {
				return result;
			}

			return o1.location.compareTo(o2.location);
		});
	}

	/**
	 * Sets the search type to either exact or partial
	 * @param exactSearch - The search type. {@code true} represents an exact search, {@code false} represents a partial search
	 */
	public void setSearchType(boolean exactSearch) {
		this.exactSearch = exactSearch;
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location - Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	public void queryJson(Path location) throws IOException {
		Map<String, List<SearchResult>> sortedResults = new TreeMap<>();

		for (var element : this.searchResults.entrySet()) {
			String query = element.getKey();
			Map<String, SearchResult> resultsMap = element.getValue();

			List<SearchResult> reusltsList = new ArrayList<>(resultsMap.values());
			sortSearchResults(reusltsList);

			sortedResults.put(query, reusltsList);
		}

		SearchResultWriter.writeSearchResults(sortedResults, location);
	}
}

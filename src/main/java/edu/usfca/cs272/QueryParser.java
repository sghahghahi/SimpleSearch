package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class QueryParser {
	/** {@code TreeMap} that stores a query string and a {@code List} of {@code SearchResult} objects */
	private final TreeMap<String, List<SearchResult>> searchResults;

	/** Initialized and populated inverted index object to reference */
	private final InvertedIndex invertedIndex;

	/** Flag to specify whether an exact search or partial search should be executed. Defaults to exact search */
	private boolean exactSearch;

	/** Class that represents a search result */
	class SearchResult {
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
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);

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

		double score = calculateScore(matches, wordCount);
		String queryString = extractQueryString(queryStems);

		List<SearchResult> searchResults = this.searchResults.get(queryString);
		if (searchResults == null) {
			searchResults = new ArrayList<>();
			this.searchResults.put(queryString, searchResults);
		}

		if (matches != 0 && score != 0) {
			SearchResult searchResult = new SearchResult(matches, score, "\"" + location.toString() + "\"");
			searchResults.add(searchResult);

			removeDuplicates(searchResults);
			sortSearchResults(searchResults);
		}
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

		double score = calculateScore(matches, wordCount);
		String queryString = extractQueryString(queryStems);

		List<SearchResult> searchResults = this.searchResults.get(queryString);
		if (searchResults == null) {
			searchResults = new ArrayList<>();
			this.searchResults.put(queryString, searchResults);
		}

		if (matches != 0 && score != 0) {
			SearchResult searchResult = new SearchResult(matches, score, "\"" + location.toString() + "\"");
			searchResults.add(searchResult);

			removeDuplicates(searchResults);
			sortSearchResults(searchResults);
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
	 * This is a slow, inefficient approach to ranking search results (removing duplicates).
	 * Ideally, we wouldn't allow duplicates when adding to the {@code List} of {@code SearchResult} objects
	 * Removes duplicates from the {@code List} of {@code SearchResult} objects
	 * @param searchResults - The {@code List} of {@code SearchResult} objects
	 */
	private static void removeDuplicates(List<SearchResult> searchResults) {
		Collections.reverse(searchResults);
		HashSet<String> seenLocations = new HashSet<>();
		searchResults.removeIf(result -> !seenLocations.add(result.location));
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
		SearchResultWriter.writeSearchResults(this.searchResults, location);
	}
}

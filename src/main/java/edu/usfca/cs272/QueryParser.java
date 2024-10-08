package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.text.DecimalFormat;

import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

public class QueryParser {
	/** Nested structure to store metadata about search results conducted on our inverted index */
	private final TreeMap<String, List<TreeMap<String, String>>> searchResults;

	/** Inverted index object to reference */
	private final InvertedIndex invertedIndex;

	/**
	 * Constrcutor that initializes our search result metadata data structure to an empty {@code TreeMap}
	 * @param invertedIndex - The inverted index object to reference. We are not constructing a new inverted index in this class.
	 *  This inverted index is passed from the caller and is assumed to be properly initialized and populated
	 */
	public QueryParser(InvertedIndex invertedIndex) {
		this.searchResults= new TreeMap<>();
		this.invertedIndex = invertedIndex;
	}

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation - Where to find the query words
	 * @param lookupLocation - Where the words associated with the query stems are located
	 * @throws IOException - If an IO error occurs
	 */
	public void queryLocation(Path queryLocation, Path lookupLocation) throws IOException {
		TreeSet<String> queryStems;
		SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);

		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				queryStems = FileStemmer.uniqueStems(line, snowballStemmer);
				search(queryStems, lookupLocation.toString());
			}
		}
	}

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems - The query stems to Stringify
	 * @return The space-separated query {@code String}
	 */
	public String extractQueryString(TreeSet<String> queryStems) {
		StringBuilder queryString = new StringBuilder();

		for (String queryStem : queryStems) {
			queryString.append(queryStem+ " ");
		}

		return queryString.toString().strip();
	}

	/**
	 * Performs a search of {@code queryStems} on the inverted index
	 * @param queryStems - The query stems to search
	 * @param location - Where the words associated with {@code queryStems} are located
	 */
	public void search(TreeSet<String> queryStems, String location) {
		int wordCount = this.invertedIndex.numCounts();

		int matches = 0;
		for (String queryStem : queryStems) {
			matches += this.invertedIndex.numLocations(queryStem);
		}

		double score = calculateScore(matches, wordCount);
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

		String queryString = extractQueryString(queryStems);

		List<TreeMap<String, String>> innerList = this.searchResults.get(queryString);
		if (innerList == null) {
			innerList = new Stack<>();
			this.searchResults.put(queryString, innerList);
		}

		TreeMap<String, String> innerMap = new TreeMap<>();
		innerMap.put("count", Integer.toString(wordCount));
		innerMap.put("score", FORMATTER.format(score));
		innerMap.put("where", "\"" + location + "\"");

		innerList.add(innerMap);
	}

	/**
	 * Calculates the score for a search result
	 * @param matches - Number of matches for the current result
	 * @param wordCount - Number of stems
	 * @return The score of the search result based on this calculation: {@code matches / wordcount}
	 */
	public double calculateScore(int matches, int wordCount) {
		return matches / wordCount;
	}

	/**
	 * Writes the search result as a pretty JSON object
	 * @param location - Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	public void queryJson(Path location) throws IOException {
		JsonWriter.writeSearchResults(this.searchResults, location);
	}

	// TODO
	@Override
	public String toString() {
		return String.format(
			""
		);
	}
}

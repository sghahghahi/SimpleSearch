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
	private final TreeMap<String, List<TreeMap<String, String>>> searchResults;

	private final InvertedIndex invertedIndex;

	public QueryParser(InvertedIndex invertedIndex) {
		this.searchResults= new TreeMap<>();
		this.invertedIndex = invertedIndex;
	}

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

	public String extractQueryString(TreeSet<String> queryStems) {
		StringBuilder queryString = new StringBuilder();

		for (String queryStem : queryStems) {
			queryString.append(queryStem+ " ");
		}

		return queryString.toString().strip();
	}

	public void search(TreeSet<String> queryStems, String location) {
		int wordCount = this.invertedIndex.numCounts();

		int matches = 0;
		for (String queryStem : queryStems) {
			matches += this.invertedIndex.numLocations(queryStem);
		}

		double score = calculateScore(matches, wordCount);
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

		String queryString = extractQueryString(queryStems);


		List<TreeMap<String, String>> innerStack = this.searchResults.get(queryString);
		if (innerStack == null) {
			innerStack = new Stack<>();
			this.searchResults.put(queryString, innerStack);
		}

		TreeMap<String, String> innerMap = new TreeMap<>();
		innerMap.put("count", Integer.toString(wordCount));
		innerMap.put("score", FORMATTER.format(score));
		innerMap.put("where", "\"" + location + "\"");

		innerStack.add(innerMap);
	}

	public double calculateScore(int matches, int wordCount) {
		return matches / wordCount;
	}

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

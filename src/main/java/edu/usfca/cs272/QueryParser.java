package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Set;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for parsing query files
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class QueryParser {
	/** {@code TreeMap} that maps each query string to a {@code Map} of locations and its {@code SearchResult} objects */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> searchResults;
	
	/*
	 * TODO Create 2 maps, one for exact, one for partial
	 */

	/** Initialized and populated inverted index object to reference */
	private final InvertedIndex invertedIndex;

	/** Flag to specify whether an exact search or partial search should be executed. Defaults to exact search */
	private boolean exactSearch; // TODO Could just make this a parameter to queryLocation and queryJson
	// TODO OR make this Function<...> searchFunction 

	/** Stemmer to use file-wide */
	private final SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH); // TODO Either make static or init in the constructor

	/**
	 * Constructor that initializes our search result metadata data structure to an empty {@code TreeMap}
	 * @param invertedIndex - The inverted index object to reference. We are not constructing a new inverted index in this class.
	 * This inverted index is passed from the caller and is assumed to be properly initialized and populated
	 */
	public QueryParser(InvertedIndex invertedIndex) {
		this.searchResults= new TreeMap<>();
		this.invertedIndex = invertedIndex;
		this.exactSearch = true;
	}

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation - Where to find the query words
	 * @throws IOException If an IO error occurs
	 */
	public void queryLocation(Path queryLocation) throws IOException { // TODO parseLocation
		TreeSet<String> queryStems;

		try (BufferedReader reader = Files.newBufferedReader(queryLocation, UTF_8)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				// TODO Move this logic into a parseLine(String line)
				queryStems = FileStemmer.uniqueStems(line, this.snowballStemmer);

				List<InvertedIndex.SearchResult> searchResults;
				// TODO searchFunction.apply
				if (this.exactSearch) {
					searchResults = this.invertedIndex.exactSearch(queryStems);
				} else {
					searchResults = this.invertedIndex.partialSearch(queryStems);
				}

				String queryString = extractQueryString(queryStems);
				if (!queryString.isBlank()) {
					this.searchResults.put(queryString, searchResults);
				}
			}
		}
	}

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems - The query stems to Stringify
	 * @return The space-separated query {@code String}
	 */
	private String extractQueryString(Set<String> queryStems) { // TODO Could be static
		return String.join(" ", queryStems);
	}

	/**
	 * Sets the search type to either exact or partial
	 * @param exactSearch - The search type. {@code true} represents an exact search, {@code false} represents a partial search
	 */
	public void setSearchType(boolean exactSearch) {
		this.exactSearch = exactSearch;
		// TODO this changes these searchFunction... if(exact) searchFunction = invertedIndex::exactSearch
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location - Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	public void queryJson(Path location) throws IOException { // TODO Need to know whether to output exact or partial results
		SearchResultWriter.writeSearchResults(this.searchResults, location);
	}
}

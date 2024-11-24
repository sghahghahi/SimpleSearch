package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Interface responsible for parsing query files
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public interface QueryParser {
	/**
	 * Sets the search mode to either exact or partial
	 * @param isExactSearch The search type. {@code true} represents an exact search, {@code false} represents a partial search
	 */
	void setSearchMode(boolean isExactSearch);

	/**
	 * Gets the search query from the passed file. Performs a search of the query words on the inverted index
	 * @param queryLocation Where to find the query words
	 * @throws IOException If an IO error occurs
	 */
	void parseLocation(Path queryLocation) throws IOException;

	/**
	 * Parses a line and performs a search on the inverted index
	 * @param line The line to parse
	 */
	void parseLine(String line);

	/**
	 * Returns a space-separated {@code String} of the query stems
	 * @param queryStems The query stems to join
	 * @return The space-separated query {@code String}
	 */
	static String extractQueryString(Set<String> queryStems) {
		return String.join(" ", queryStems);
	}

	/**
	 * Writes the search results as pretty JSON objects
	 * @param location Where to write the results to
	 * @throws IOException If an IO error occurs
	 */
	void queryJson(Path location) throws IOException;

	/**
	 * Returns a {@code Set} of the query strings in the result map
	 * @return A {@code Set} of the query strings in the reuslt map
	 */
	Set<String> getQueryStrings();

	/**
	 * Returns the search type. {@code true} for exact, or {@code false} for partial.
	 * @return the search type. {@code true} for exact, or {@code false} for partial.
	 */
	boolean getSearchType();

	/**
	 * Returns a {@code List} of {@link InvertedIndex.SearchResult} objects for a particular {@code queryString}
	 * @param queryString The query string to look up in the {@code List} of search results
	 * @return A {@code List} of {@link InvertedIndex.SearchResult} objects for a particular {@code queryString} or an empty list
	 * if the {@code queryString} is not in the {@code Map} of search results
	 */
	List<InvertedIndex.SearchResult> getSearchResults(String queryString);

	/**
	 * Checks if {@code queryString} is a key in the results map
	 *
	 * @see #getSearchResults(String)
	 *
	 * @param queryString The query string to check
	 * @return {@code true} if {@code queryString} is a key in the result map
	 */
	default boolean containsQueryString(String queryString) {
		return getSearchResults(queryString).size() > 0;
	}

	/**
	 * Returns the number of query strings in the reuslt map
	 * @return The number of query strings in the result map
	 */
	int numQueryStrings();

	/**
	 * Returns the number of search results for a specific query string
	 * @param queryString The query string to look up in the result map
	 * @return The number of search results for {@code queryString}
	 */
	default int numSearchResults(String queryString) {
		return getSearchResults(queryString).size();
	}
}

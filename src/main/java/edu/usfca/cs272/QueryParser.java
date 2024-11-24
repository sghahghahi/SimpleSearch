package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** TODO */
// TODO Javadoc goes here (in the interface)
public interface QueryParser {
	/**
	 * TODO
	 * @param isExactSearch
	 */
	void setSearchMode(boolean isExactSearch);

	/**
	 * TODO
	 * @param queryLocation
	 * @throws IOException
	 */
	void parseLocation(Path queryLocation) throws IOException;

	/**
	 * TODO
	 * @param line
	 */
	void parseLine(String line);

	/**
	 * TODO
	 * @param queryStems
	 * @return
	 */
	static String extractQueryString(Set<String> queryStems) {
		return String.join(" ", queryStems);
	}

	/**
	 * TODO
	 * @param location
	 * @throws IOException
	 */
	void queryJson(Path location) throws IOException;

	/**
	 * TODO
	 * @return
	 */
	Set<String> getQueryStrings();

	/**
	 * TODO
	 * @return
	 */
	boolean getSearchType();

	/**
	 * TODO
	 * @param queryString
	 * @return
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
	 * TODO
	 * @return
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

package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** TODO */
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
	 * TODO
	 * @param queryString
	 * @return
	 */
	boolean containsQueryString(String queryString);

	/**
	 * TODO
	 * @return
	 */
	int numQueryStrings();

	/**
	 * TODO
	 * @param queryString
	 * @return
	 */
	int numSearchResults(String queryString);
}

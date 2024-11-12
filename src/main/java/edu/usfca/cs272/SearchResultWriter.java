package edu.usfca.cs272;

import static edu.usfca.cs272.JsonWriter.*;
import edu.usfca.cs272.InvertedIndex.SearchResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.text.DecimalFormat;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class responsible for writing {@code SearchResult} objects as pretty JSON objects
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class SearchResultWriter {

	/** Format {@code double}s to 8 decimals */
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

	/**
	 * Writes search results as pretty JSON obejcts
	 * @param searchResults - The search results to write
	 * @param writer - The writer to use
	 * @param indent - The indentation level
	 * @throws IOException If an IO error occurs
	 */
	public static void writeSearchResults(Map<String, ? extends Collection<SearchResult>> searchResults, Writer writer, int indent) throws IOException {
		writeIndent("{", writer, 0);

		var iterator = searchResults.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeEntry(element.getKey(), element.getValue(), writer, indent);
		}

		while (iterator.hasNext()) {
			writeIndent(",", writer, 0);
			var element = iterator.next();
			writeEntry(element.getKey(), element.getValue(), writer, indent);
		}

		writeIndent("\n", writer, 0);
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes search results as pretty JSON objects
	 * @param searchResults - The search results to write
	 * @param location - Where to write the search results
	 * @throws IOException If an IO error occurs
	 */
	public static void writeSearchResults(Map<String, ? extends Collection<SearchResult>> searchResults, Path location) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(location, UTF_8)) {
			writeSearchResults(searchResults, writer, 0);
		}
	}

	/**
	 * Writes the {@code key} and {@code values} as a key/value pair separated by a {@code :} as a pretty JSON object
	 * @param key - The query string to write
	 * @param results - The search results corresponding to the {@code key}
	 * @param writer - The writer to use
	 * @param indent - The indentation level
	 * @throws IOException If an IO error occurs
	 */
	public static void writeEntry(String key, Collection<SearchResult> results, Writer writer, int indent) throws IOException {
		writeIndent("\n", writer, 0);
		writeQuote(key, writer, indent + 1);
		writeIndent(": ", writer, 0);
		writeIndent("[", writer, 0);

		if (!results.isEmpty()) {
			writeList(results, writer, indent + 1);
		} else {
			writeIndent("\n", writer, 0);
			writeIndent("]", writer, indent + 1);
		}
	}

	/**
	 * Writes a {@code List} of search results as pretty JSON objects
	 * @param results - The search results to write
	 * @param writer - The writer to use
	 * @param indent - The indentation level
	 * @throws IOException If an IO error occurs
	 */
	public  static void writeList(Collection<SearchResult> results, Writer writer, int indent) throws IOException {
		var iterator = results.iterator();
		if (iterator.hasNext()) {
			writeIndent("\n", writer, 0);
			writeSearchResult(iterator.next(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writeIndent(",\n", writer, 0);
			writeSearchResult(iterator.next(), writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes an individual search result as a pretty JSON object
	 * @param result - The search result to write
	 * @param writer - The writer to use
	 * @param indent - The indentation level
	 * @throws IOException If an IO error occurs
	 */
	public  static void writeSearchResult(SearchResult result, Writer writer, int indent) throws IOException {
		TreeMap<String, String> map = new TreeMap<>();
		map.put("count", Integer.toString(result.getCount()));
		map.put("score", FORMATTER.format(result.getScore()));
		map.put("where", "\"" + result.getLocation()+ "\"");
		writeStringObject(map, writer, indent);
	}

	/** Default constructor to prevent instantiation since all methods are {@code static} */
	private SearchResultWriter() {}
}

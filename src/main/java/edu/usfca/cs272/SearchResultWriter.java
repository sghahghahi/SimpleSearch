package edu.usfca.cs272;

import static edu.usfca.cs272.JsonWriter.*;
import edu.usfca.cs272.QueryParser.SearchResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;
import java.util.TreeMap;

public class SearchResultWriter {

	/** Format {@code double}s to 8 decimals */
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

	/**
	 * TODO
	 * @param searchResults
	 * @param writer
	 * @param indent
	 * @throws IOException
	 */
	public static void writeSearchResults(TreeMap<String, List<SearchResult>> searchResults, Writer writer, int indent) throws IOException {
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
	 * TODO
	 * @param searchResults
	 * @param location
	 * @throws IOException
	 */
	public static void writeSearchResults(TreeMap<String, List<QueryParser.SearchResult>> searchResults, Path location) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(location, UTF_8)) {
			writeSearchResults(searchResults, writer, 0);
		}
	}

	/**
	 * TODO
	 * @param key
	 * @param results
	 * @param writer
	 * @param indent
	 * @throws IOException
	 */
	private static void writeEntry(String key, List<SearchResult> results, Writer writer, int indent) throws IOException {
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
	 * TODO
	 * @param results
	 * @param writer
	 * @param indent
	 * @throws IOException
	 */
	private static void writeList(List<SearchResult> results, Writer writer, int indent) throws IOException {
		var iterator = results.iterator();
		while (iterator.hasNext()) {
			writeIndent("\n", writer, 0);
			writeSearchResult(iterator.next(), writer, indent + 1);

			if (iterator.hasNext()) {
				writeIndent(",", writer, 0);
			}
		}

		writeIndent("\n", writer, 0);
		writeIndent("]", writer, indent);
	}

	/**
	 * TODO
	 * @param result
	 * @param writer
	 * @param indent
	 * @throws IOException
	 */
	private static void writeSearchResult(SearchResult result, Writer writer, int indent) throws IOException {
		TreeMap<String, String> map = new TreeMap<>();
		map.put("count", Integer.toString(result.count));
		map.put("score", FORMATTER.format(result.score));
		map.put("where", result.location);
		writeStringObject(map, writer, indent);
	}

	/** Default constructor to prevent instantiation since all methods are {@code static} */
	private SearchResultWriter() {}
}

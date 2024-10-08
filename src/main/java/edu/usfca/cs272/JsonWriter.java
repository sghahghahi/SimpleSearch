package edu.usfca.cs272;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Writer writer, int indent) throws IOException {

		writeIndent("[", writer, 0);

		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writeIndent("\n", writer, 0);
			writeIndent(iterator.next().toString(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			String element = iterator.next().toString();
			writeIndent(",\n", writer, 0);
			writeIndent(element, writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	public static void writeObject(TreeMap<String, String> elements, Writer writer, int indent) throws IOException {
		writeIndent("{", writer, indent);

		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent("\n", writer, 0);
			writeQuote(element.getKey(), writer, indent + 1);
			writeIndent(": ", writer,  0);
			writeIndent(element.getValue(), writer, 0);
		}

		while (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(",\n", writer, 0);
			writeQuote(element.getKey(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeIndent(element.getValue(), writer, 0);
		}

		writeIndent("\n", writer, 0);
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Writer writer, int indent) throws IOException {

		writeIndent("{", writer, 0);

		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent("\n", writer, 0);
			writeQuote(element.getKey().toString(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeIndent(element.getValue().toString(), writer, 0);
		}

		while (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(",\n", writer, 0);
			writeQuote(element.getKey().toString(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeIndent(element.getValue().toString(), writer, 0);
		}

		writeIndent("\n", writer, 0);
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectArrays(
			Map<String, ? extends Collection<? extends Number>> elements,
			Writer writer, int indent) throws IOException {


		writeIndent("{", writer, 0);

		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent("\n", writer, 0);
			writeQuote(element.getKey().toString(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeArray(element.getValue(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(",\n", writer, 0);
			writeQuote(element.getKey().toString(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeArray(element.getValue(), writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(
			Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(
			Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	public static void writeArrayObjects(List<TreeMap<String, String>> elements, Writer writer, int indent) throws IOException {
		writeIndent("[", writer, 0);

		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writeIndent("\n", writer, 0);
			writeObject(iterator.next(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			writeIndent(",\n", writer, 0);
			writeObject(iterator.next(), writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArrayObjects(
			Collection<? extends Map<String, ? extends Number>> elements,
			Writer writer, int indent) throws IOException {

		writeIndent("[", writer, 0);

		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent("\n", writer, 0);
			writeObject(element, writer, indent + 1);
		}

		while (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(",\n", writer, 0);
			writeObject(element, writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(
			Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(
			Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested Maps to file.
	 * @param elements The elements to write
	 * @param writer The writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 * inner elements are indented by one, and the last bracket is indented at
	 * the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObjectObject(Map<String, ? extends AbstractMap<String, TreeSet<Integer>>> elements, Writer writer, int indent) throws IOException {
		writeIndent("{", writer, 0);

		var iterator = elements.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent("\n", writer, 0);
			writeQuote(element.getKey(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeObjectArrays(element.getValue(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(",\n", writer, 0);
			writeQuote(element.getKey(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeObjectArrays(element.getValue(), writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON array with nested Maps to file.
	 * @param elements The elements to write
	 * @param path The file path to write to
	 * @throws IOException If an IO error occurs
	 */
	public static void writeObjectObject(Map<String, ? extends AbstractMap<String, TreeSet<Integer>>> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectObject(elements, writer, 0);
		}
	}

	public static void writeSearchResults(Map<String, List<TreeMap<String, String>>> searchResults, Writer writer, int indent) throws IOException {
		writeIndent("{", writer, 0);

		var iterator = searchResults.entrySet().iterator();
		if (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent("\n", writer, 0);
			writeQuote(element.getKey(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeArrayObjects(element.getValue(), writer, indent + 1);
		}

		while (iterator.hasNext()) {
			var element = iterator.next();
			writeIndent(",\n", writer, 0);
			writeQuote(element.getKey(), writer, indent + 1);
			writeIndent(": ", writer, 0);
			writeArrayObjects(element.getValue(), writer, indent + 1);
		}

		writeIndent("\n", writer, 0);
		writeIndent("}", writer, indent);
	}

	public static void writeSearchResults(Map<String, List<TreeMap<String, String>>> searchResults, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeSearchResults(searchResults, writer, 0);
		}
	}

	/** No need to instantiate this class because all methods are {@code static} */
	private JsonWriter() {}
}

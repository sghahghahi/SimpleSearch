package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing, cleaning, and stemming text and text files into
 * collections of processed words.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class FileStemmer {
	/** Regular expression that matches any whitespace. **/
	public static final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\p{Space}+");

	/** Regular expression that matches non-alphabetic characters. **/
	public static final Pattern CLEAN_REGEX = Pattern.compile("(?U)[^\\p{Alpha}\\p{Space}]+");

	/** Reusable empty array. */
	public static final String[] EMPTY = new String[0];

	/**
	 * Cleans the text by removing any non-alphabetic characters (e.g. non-letters
	 * like digits, punctuation, symbols, and diacritical marks like the umlaut) and
	 * converting the remaining characters to lowercase.
	 *
	 * @param text the text to clean
	 * @return cleaned text
	 */
	public static String clean(String text) {
		String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD);
		cleaned = CLEAN_REGEX.matcher(cleaned).replaceAll("");
		return cleaned.toLowerCase();
	}

	/**
	 * Splits the supplied text by whitespaces.
	 *
	 * @param text the text to split
	 * @return an array of {@link String} objects
	 */
	public static String[] split(String text) {
		return text.isBlank() ? EMPTY : SPLIT_REGEX.split(text.strip());
	}

	/**
	 * Parses the text into an array of clean words.
	 *
	 * @param text the text to clean and split
	 * @return an array of {@link String} objects
	 *
	 * @see #clean(String)
	 * @see #parse(String)
	 */
	public static String[] parse(String text) {
		return split(clean(text));
	}

	/**
	 * Parses the line into cleaned and stemmed words and adds them to the provided
	 * collection.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @param stems the collection to add stems
	 *
	 * @throws NullPointerException if {@link Collection#add(Object)} throws it
	 * @throws UnsupportedOperationException if {@link Collection#add(Object)} throws it
	 * @throws ClassCastException if {@link Collection#add(Object)} throws it
	 * @throws IllegalArgumentException if {@link Collection#add(Object)} throws it
	 * or if any of the parameters are {@code null}
	 * @throws IllegalStateException if {@link Collection#add(Object)} throws it
	 */
	public static void addStems(String line, Stemmer stemmer, Collection<String> stems)
		throws NullPointerException,
		UnsupportedOperationException,
		ClassCastException,
		IllegalArgumentException,
		IllegalStateException {

		if (
			line == null ||
			stemmer == null ||
			stems == null
		) { throw new IllegalArgumentException("Cannot operate on null parameter"); }

		final String[] cleanWords = parse(line);
		for (String cleanWord : cleanWords) {
			stems.add(
				stemmer.stem(cleanWord)
				.toString()
			);
		}
	}

	/**
	 * Parses the line into a list of cleaned and stemmed words.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words in parsed order
	 */
	public static ArrayList<String> listStems(String line, Stemmer stemmer) {
		final ArrayList<String> stems = new ArrayList<>();
		addStems(line, stemmer, stems);

		return stems;
	}

	/**
	 * Parses the line into a list of cleaned and stemmed words using the default
	 * stemmer for English.
	 *
	 * @param line the line of words to parse and stem
	 * @return a list of cleaned and stemmed words in parsed order
	 */
	public static ArrayList<String> listStems(String line) {
		return listStems(line, new SnowballStemmer(ENGLISH));
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words
	 * using the default stemmer for English.
	 *
	 * @param input the input file to parse and stem
	 * @return a list of stems from file in parsed order
	 * @throws IOException if unable to read or parse file
	 * @throws NullPointerException if {@link ArrayList#addAll(Collection)} throws it
	 */
	public static ArrayList<String> listStems(Path input)
		throws IOException, NullPointerException {

		try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
			final SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
			final ArrayList<String> stems = new ArrayList<>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				stems.addAll(
					listStems(line, snowballStemmer)
				);
			}

			return stems;
		}
	}

	/**
	 * Parses the line into a set of unique, sorted, cleaned, and stemmed words.
	 *
	 * @param line the line of words to parse and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
		final TreeSet<String> stems = new TreeSet<>();
		addStems(line, stemmer, stems);

		return stems;
	}

	/**
	 * Parses the line into a set of unique, sorted, cleaned, and stemmed words
	 * using the default stemmer for English.
	 *
	 * @param line the line of words to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 */
	public static TreeSet<String> uniqueStems(String line) {
		return uniqueStems(line, new SnowballStemmer(ENGLISH));
	}

	/**
	 * Reads a file line by line, parses each line into a set of unique, sorted,
	 * cleaned, and stemmed words using the default stemmer for English.
	 *
	 * @param input the input file to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words from file
	 * @throws IOException if unable to read or parse file
	 * @throws NullPointerException if {@link TreeSet#addAll(Collection)} throws it
	 */
	public static TreeSet<String> uniqueStems(Path input)
		throws IOException,
		NullPointerException {

		try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
			final SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
			final TreeSet<String> stems = new TreeSet<>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				stems.addAll(
					uniqueStems(line, snowballStemmer)
				);
			}

			return stems;
		}
	}

	/**
	 * Reads a file line by line, parses each line into unique, sorted, cleaned, and
	 * stemmed words using the default stemmer for English, and adds the set of
	 * unique sorted stems to a list per line in the file.
	 *
	 * @param input the input file to parse and stem
	 * @return a list where each item is the sets of unique sorted stems parsed from
	 *   a single line of the input file
	 * @throws IOException if unable to read or parse file
	 */
	public static ArrayList<TreeSet<String>> listUniqueStems(Path input) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
			final SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
			final ArrayList<TreeSet<String>> stems = new ArrayList<>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				stems.add(
					uniqueStems(line, snowballStemmer)
				);
			}

			return stems;
		}
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 * @throws IOException if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		// demonstrates how to use split, clean, and parse
		System.out.println("____PARSING DEMO____");
		System.out.println();

		String sally = """
				Sally Sue...\t sells 76 sea-shells
				at THE sEa_shorE soirée!""";

		System.out.println("Original:");
		System.out.println(sally);
		System.out.println();

		System.out.println("Cleaned:");
		System.out.println(clean(sally));
		System.out.println();

		System.out.println(" Split: " + Arrays.toString(split(sally)));
		System.out.println("Parsed: " + Arrays.toString(parse(sally)));
		System.out.println();

		// demonstrates how to use stemmer
		System.out.println("____STEMMER DEMO____");
		System.out.println();

		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		String demo = "practicing";
		String stem = stemmer.stem(demo).toString();

		System.out.println("Word: " + demo);
		System.out.println("Stem: " + stem);
		System.out.println();

		// demonstrates how to use list/uniqueStems methods
		System.out.println("____STEMMING TEXT____");
		System.out.println();

		String practice = """
				practic practical practice practiced practicer practices
				practicing practis practisants practise practised practiser
				practisers practises practising practitioner practitioners
				""";

		System.out.println("Original: \n" + practice);
		System.out.println("  List: " + listStems(practice));
		System.out.println("Unique: " + uniqueStems(practice));
		System.out.println();

		// demonstrates stemming files
		System.out.println("____STEMMING FILE____");
		System.out.println();

		Path base = Path.of("src", "test", "resources", "stemmer");
		Path file = base.resolve("cleaner.txt");
		String input = Files.readString(file, UTF_8);

		System.out.println("Original:\n" + input);

		System.out.println("       List: " + listStems(file));
		System.out.println("     Unique: " + uniqueStems(file));
		System.out.println("List Unique: " + listUniqueStems(file));
	}

	/** Prevent instantiating this class of static methods. */
	private FileStemmer() {
	}
}

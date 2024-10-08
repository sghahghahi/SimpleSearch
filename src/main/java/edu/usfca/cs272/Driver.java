package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class Driver {

	/** {@code -text} flag passed an argument to this program. Next argument signifies file path to read text from. */
	public static final String TEXT = "-text";

	/** {@code -counts} flag passed as an argument to this program. File path to write word counts to (next argument) not necessary. */
	public static final String COUNTS = "-counts";

	/** {@code -index} flag passed as an argument to this program. File path to write inverted index to (next argument) not necessary. */
	public static final String INDEX = "-index";

	/** Represents the current working directory. Useful for creating new files. */
	public static final String CURR_DIR = ".";

	/** File name to write word counts to if no file path included after {@code -counts} flag. Will write file in the current working directory. */
	public static final String COUNTS_BACKUP = "counts.json";

	/** File name to write inverted index to if no file path included after {@code -index} flag. Will write file in the current working directory. */
	public static final String INDEX_BACKUP = "index.json";

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		ArgumentParser argParser = new ArgumentParser(args);
		InvertedIndex invertedIndex = new InvertedIndex();
		Path location;

		if (argParser.hasFlag(TEXT)) {
			location = argParser.getPath(TEXT);
			try {
				TextFileIndexer.indexLocation(location, invertedIndex);
			} catch (IOException e) {
				System.err.println("Unable to index the files from location: " + location);
			} catch (NullPointerException e) {
				System.err.println("No input file was provided after '-text' flag.");
			}
		}

		if (argParser.hasFlag(COUNTS)) {
			location = argParser.getPath(COUNTS, Path.of(CURR_DIR, COUNTS_BACKUP));
			try {
				invertedIndex.indexCounts(location);
			} catch (IOException e) {
				System.err.println("Unable to write word counts to location: " + location);
			}
		}

		if (argParser.hasFlag(INDEX)) {
			location = argParser.getPath(INDEX, Path.of(CURR_DIR, INDEX_BACKUP));
			try {
				invertedIndex.indexJson(location);
			} catch (IOException e) {
				System.err.println("Unable to write inverted index to location: " + location);
			}
		}
	}
}

/* TODO
Description	Resource	Path	Location	Type
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 146	Java Problem
Javadoc: Missing comment for public declaration	InvertedIndex.java	/SearchEngine/src/main/java/edu/usfca/cs272	line 152	Java Problem
*/
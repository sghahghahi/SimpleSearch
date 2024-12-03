package edu.usfca.cs272;

import java.io.IOException;
import java.net.URISyntaxException;
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

	/** {@code -query} flag passed as an argument to this program. File path to query (next argument). Will trigger a search for each of the multi-line queries in the file. */
	public static final String QUERY = "-query";

	/** {@code -results} flag passed as an argument to this program. File path to write search results to (next argument) not necessary. */
	public static final String RESULTS = "-results";

	/** File name to write search results to if no file path included after {@code -results} flag. Will write this file in the current working directory. */
	public static final String RESULTS_BACKUP = "results.json";

	/** {@code -partial} flag passed as an argument to this program. Will trigger a partial search for each of the multi-line queries in the query file. */
	public static final String PARTIAL = "-partial";

	/** {@code -threads} flag passed as an argument to this program. Will trigger a build of a thread-safe inverted index. */
	public static final String THREAD = "-threads";

	/** If the user doesn't specify how many threads to use to build a thread-safe inverted index, this default value will be used. */
	public static final int NUM_THREADS = 5;

	/** {@code -html} flag passed as an argument to this program. Enables multithreading. Next argument (required) is the seed URI the web crawler should download and process to build the inverted index. */
	public static final String HTML = "-html";

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		ArgumentParser argParser = new ArgumentParser(args);
		WorkQueue workQueue = null;
		InvertedIndex invertedIndex;
		TextFileIndexer textFileIndexer;
		QueryParser queryParser;
		WebCrawler crawler = null;

		Path location;

		if (argParser.hasFlag(THREAD) || argParser.hasFlag(HTML)) {
			workQueue = new WorkQueue(argParser.getInteger(THREAD, NUM_THREADS));
			ThreadSafeInvertedIndex safeIndex = new ThreadSafeInvertedIndex();
			invertedIndex = safeIndex;
			textFileIndexer = new ThreadSafeTextFileIndexer(safeIndex, workQueue);
			queryParser = new ThreadSafeQueryParser(safeIndex, workQueue);
			String seedURI = argParser.getString(HTML);
			if (argParser.hasFlag(HTML)) {
				try {
					crawler = new WebCrawler(safeIndex, LinkFinder.toUri(seedURI));
				} catch (URISyntaxException e) {
					System.err.printf("Unable to create web crawler from %s\n", seedURI);
				} catch (NullPointerException e) {
					System.err.println("No seed file was provided after the '-html' flag.");
				}
			}

		} else {
			invertedIndex = new InvertedIndex();
			textFileIndexer = new TextFileIndexer(invertedIndex);
			queryParser = new DefaultQueryParser(invertedIndex);
		}

		if (argParser.hasFlag(TEXT)) {
			location = argParser.getPath(TEXT);
			try {
				textFileIndexer.indexLocation(location);
			} catch (IOException e) {
				System.err.printf("Unable to index the files from location: %s\n", location);
			} catch (NullPointerException e) {
				System.err.println("No input file was provided after the '-text' flag.");
			}
		}

		if (crawler != null) {
			crawler.crawl();
		}

		if (argParser.hasFlag(QUERY)) {
			location = argParser.getPath(QUERY);
			try {
				queryParser.setSearchMode(!argParser.hasFlag(PARTIAL));
				queryParser.parseLocation(location);
			} catch (IOException e) {
				System.err.printf("Unable to read search queries from location: %s\n", location);
			} catch (NullPointerException e) {
				System.err.println("No input file was provided after '-query' flag.");
			}
		}

		if (workQueue != null) {
			workQueue.shutdown();
		}

		if (argParser.hasFlag(COUNTS)) {
			location = argParser.getPath(COUNTS, Path.of(CURR_DIR, COUNTS_BACKUP));
			try {
				invertedIndex.indexCounts(location);
			} catch (IOException e) {
				System.err.printf("Unable to write word counts to location: %s\n", location);
			}
		}

		if (argParser.hasFlag(INDEX)) {
			location = argParser.getPath(INDEX, Path.of(CURR_DIR, INDEX_BACKUP));
			try {
				invertedIndex.indexJson(location);
			} catch (IOException e) {
				System.err.printf("Unable to write inverted index to location: %s\n", location);
			}
		}

		if (argParser.hasFlag(RESULTS)) {
			location = argParser.getPath(RESULTS, Path.of(CURR_DIR, RESULTS_BACKUP));
			try {
				queryParser.queryJson(location);
			} catch (IOException e) {
				System.err.printf("Unable to write search results to location: %s\n", location);
			}
		}

		if (workQueue != null) {
			workQueue.join();
		}
	}
}

package edu.usfca.cs272;

import java.net.URI;
import java.util.ArrayList;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for web crawling starting from a specific seed URI.
 * Will only redirect up to three times.
 * Builds an inverted index from the seed URI.
 * This class is thread-safe.
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class WebCrawler {
	/** The inverted index to add to */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** The initial {@code URI} to build an inverted index from */
	private final URI seedURI;

	/** The maximum number of redirects allowed */
	private static final int MAX_REDIRECTS = 3;

	/** The stemmer to use class-wide */
	private final SnowballStemmer snowballStemmer;

	/** The number of URLs to crawl */
	private final int numCrawls;

	/** The work queue to assign tasks to */
	private final WorkQueue queue;

	/**
	 * Constructs a web cralwer with a thread-safe inverted index and seed URI
	 * @param invertedIndex The inverted index to add to
	 * @param seedURI The seed URI to build the inverted index from
	 * @param numCrawls The number of URLs to crawl
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, URI seedURI, int numCrawls, WorkQueue queue) {
		this.invertedIndex = invertedIndex;
		this.seedURI = seedURI;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
		this.numCrawls = numCrawls;
		this.queue = queue;
	}

	/** Nested class that represents a task for a thread to do */
	private class Work implements Runnable {
		/** TODO members */
		private final URI seed;

		/**
		 * TODO
		 * @param seed
		 */
		public Work(URI seed) {
			this.seed = seed;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'run'");
		}
	}

	/**
	 * Starts crawling from the seed URI.
	 * Will redirect up to three times.
	 * Adds words and their locations to the inverted index.
	 */
	public void crawl() {
		String html = HtmlFetcher.fetch(this.seedURI, MAX_REDIRECTS);
		if (html == null) {
			System.err.printf("Could not start crawl at %s\n", this.seedURI);
			return;
		}

		html = HtmlCleaner.stripBlockElements(html);
		String cleanedHtml = HtmlCleaner.stripTags(html);
		cleanedHtml = HtmlCleaner.stripEntities(cleanedHtml);

		ArrayList<String> stems = FileStemmer.listStems(cleanedHtml, this.snowballStemmer);
		String cleanedURI = LinkFinder.clean(this.seedURI).toString();
		this.invertedIndex.addWords(stems, cleanedURI, 1);
	}
}

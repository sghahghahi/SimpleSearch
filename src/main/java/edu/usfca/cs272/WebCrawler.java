package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.net.URI;
import java.util.ArrayList;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class responsible for web crawling starting from a specific seed URI.
 * Will only redirect up to three times.
 * Builds an inverted index from the seed URI.
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

	/**
	 * Constructs a web cralwer with a thread-safe inverted index and seed URI
	 * @param invertedIndex The inverted index to add to
	 * @param seedURI The seed URI to build the inverted index from
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, URI seedURI) {
		this.invertedIndex = invertedIndex;
		this.seedURI = seedURI;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
	}

	/**
	 * Starts crawling from the seed URI.
	 * Will redirect up to three times.
	 * Adds words and their locations to the inverted index
	 */
	public void crawl() {
		String html = HtmlFetcher.fetch(this.seedURI, MAX_REDIRECTS);
		if (html == null) {
			System.out.printf("Could not start crawl at %s\n", this.seedURI);
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

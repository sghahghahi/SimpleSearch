package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.net.URI;
import java.util.ArrayList;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** TODO */
public class WebCrawler {
	/** TODO  */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** TODO */
	private final URI seedURI;

	/** TODO */
	private static final int MAX_REDIRECTS = 3;

	/** TODO */
	private final SnowballStemmer snowballStemmer;

	/**
	 * TODO
	 * @param invertedIndex
	 * @param seedURI
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, URI seedURI) {
		this.invertedIndex = invertedIndex;
		this.seedURI = seedURI;
		this.snowballStemmer = new SnowballStemmer(ENGLISH);
	}

	/**
	 * TODO
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

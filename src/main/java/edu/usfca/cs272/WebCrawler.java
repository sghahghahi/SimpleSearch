package edu.usfca.cs272;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for web crawling starting from a specific seed URI.
 * Will only redirect up to three times.
 * Builds an inverted index from the seed URI.
 * This class is thread safe.
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

	/** The number of URLs to crawl */
	private final int numCrawls;

	/** The work queue to assign tasks to */
	private final WorkQueue queue;

	/** {@code Set} to keep track of already crawled links */
	private final HashSet<URI> crawledLinks;

	/**
	 * Constructs a web cralwer with a thread-safe inverted index and seed URI
	 * @param invertedIndex The inverted index to add to
	 * @param seedURI The seed URI to build the inverted index from
	 * @param numCrawls The number of URLs to crawl
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, URI seedURI, int numCrawls, WorkQueue queue) {
		this.invertedIndex = invertedIndex;
		this.seedURI = seedURI;
		this.numCrawls = numCrawls;
		this.queue = queue;
		this.crawledLinks = new HashSet<>();
		this.crawledLinks.add(this.seedURI);
	}

	/** Nested class that represents a task for a thread to do */
	private class Work implements Runnable {
		/** The link to process */
		private final URI link;

		/**
		 * The link to download, process, and add to the inverted index
		 * @param link
		 */
		public Work(URI link) {
			this.link = link;
		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(this.link, MAX_REDIRECTS);

			if (html == null) {
				System.err.printf("Could not start crawl at %s\n", this.link);
				return;
			}

			html = HtmlCleaner.stripBlockElements(html);
			ArrayList<URI> hyperlinks = LinkFinder.listUris(link, html);

			synchronized (crawledLinks) {
				for (URI hyperLink : hyperlinks) {
					if (crawledLinks.size() >= numCrawls) {
						break;
					}

					if (crawledLinks.add(hyperLink)) {
						queue.execute(new Work(hyperLink));
					}
				}
			}

			String cleanedHtml = HtmlCleaner.stripTags(html);
			cleanedHtml = HtmlCleaner.stripEntities(cleanedHtml);

			SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
			ArrayList<String> stems = FileStemmer.listStems(cleanedHtml, snowballStemmer);
			URI absoulteURI = LinkFinder.toAbsolute(seedURI, this.link.toString());
			if (absoulteURI == null) {
				System.err.println("Could not make URI absoulte");
				return;
			}

			InvertedIndex localIndex = new InvertedIndex();
			localIndex.addWords(stems, absoulteURI.toString(), 1);

			// No need to synchronize because invertedIndex is thread safe
			invertedIndex.addAll(localIndex);
		}
	}

	/**
	 * Starts crawling from the seed URI.
	 * Will redirect up to three times.
	 * Adds words and their locations to the inverted index.
	 */
	public void crawl() {
		this.queue.execute(new Work(this.seedURI));
		this.queue.finish();
	}

	@Override
	public String toString() {
		return String.format("Web crawler started at %s\n", this.seedURI);
	}
}

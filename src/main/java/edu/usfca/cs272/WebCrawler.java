package edu.usfca.cs272;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Class responsible for web crawling starting from a specific seed URI.
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

	/** The maximum number of redirects allowed */
	private static final int MAX_REDIRECTS = 3;

	/** The work queue to assign tasks to */
	private final WorkQueue queue;

	/** {@code Set} to keep track of already crawled links */
	private final HashSet<URI> crawledLinks;

	/**
	 * Constructs a web crawler with a thread-safe inverted index and work queue
	 * @param invertedIndex The inverted index to add to
	 * @param queue The work queue to assign tasks to
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, WorkQueue queue) {
		this.invertedIndex = invertedIndex;
		this.queue = queue;
		this.crawledLinks = new HashSet<>();
	}

	/** Nested class that represents a task for a thread to do */
	private class Work implements Runnable {
		/** The link to process */
		private final URI link;

		/** The maximum number of URLs to crawl */
		private final int maxCrawls;

		/**
		 * Constructs a task for a thread to do
		 * @param link The link to download, process, and add to the inverted index
		 * @param maxCrawls The maximum number of URLs to crawl
		 */
		public Work(URI link, int maxCrawls) {
			this.maxCrawls = maxCrawls;
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
					if (crawledLinks.size() >= maxCrawls) {
						break;
					}

					if (crawledLinks.add(hyperLink)) {
						queue.execute(new Work(hyperLink, this.maxCrawls));
					}
				}
			}

			String cleanedHtml = HtmlCleaner.stripTags(html);
			cleanedHtml = HtmlCleaner.stripEntities(cleanedHtml);

			SnowballStemmer snowballStemmer = new SnowballStemmer(ENGLISH);
			ArrayList<String> stems = FileStemmer.listStems(cleanedHtml, snowballStemmer);

			InvertedIndex localIndex = new InvertedIndex();
			localIndex.addWords(stems, this.link.toString(), 1);

			// No need to synchronize because invertedIndex is thread safe
			invertedIndex.addAll(localIndex);
		}
	}

	/**
	 * Starts crawling from the seed URI.
	 * Adds words and their locations to the inverted index.
	 * @param seedURI The initial {@code URI} to build an inverted index from
	 * @param maxCrawls The maximum number of URLs to crawl
	 */
	public void crawl(URI seedURI, int maxCrawls) {
		this.crawledLinks.add(seedURI);
		this.queue.execute(new Work(seedURI, maxCrawls));
		this.queue.finish();
	}

	@Override
	public String toString() {
		return String.format(
			"Web crawler started at %s and has crawled %d %s\n",
			this.crawledLinks.isEmpty() ? "N/A" : this.crawledLinks.iterator().next(),
			this.crawledLinks.size(),
			this.crawledLinks.size() == 1 ? "URL" : "URLs"
		);
	}
}

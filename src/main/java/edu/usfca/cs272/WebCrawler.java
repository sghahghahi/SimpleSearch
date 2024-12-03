package edu.usfca.cs272;

import edu.usfca.cs272.HttpsFetcher;
import edu.usfca.cs272.HtmlFetcher;
import edu.usfca.cs272.HtmlCleaner;


import java.io.IOException;
import java.net.URISyntaxException;

/** TODO */
public class WebCrawler {
	/** TODO  */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** TODO */
	private final String seedURI;

	/**
	 * TODO
	 * @param invertedIndex
	 */
	public WebCrawler(ThreadSafeInvertedIndex invertedIndex, String seedURI) throws URISyntaxException, IOException {
		this.invertedIndex = invertedIndex;
		this.seedURI = seedURI;
	}
}

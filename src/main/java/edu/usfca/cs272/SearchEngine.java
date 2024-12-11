package edu.usfca.cs272;

import java.nio.file.Path;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;


/**
 * Launches a multithreaded web server and search engine.
 * Searches through an already-populated inverted index to list search results to the user.
 *
 * @author Shyon Ghahghahi
 * @version Fall 2024
 */
public class SearchEngine {
	/** The inverted index to search through */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** The port to use */
	private final int port;

	/** Path for all HTML files */
	public static final Path template = Path.of("project-sghahghahi", "src", "main", "resources");

	/**
	 * Constructs a {@code SearchEngine} object with a thread safe inverted index and port number.
	 * @param invertedIndex The inverted index to search through
	 * @param port The port to use
	 */
	public SearchEngine(ThreadSafeInvertedIndex invertedIndex, int port) {
		this.invertedIndex = invertedIndex;
		this.port = port;
	}

	/**
	 * Launches the web server and search engine at the port specified when this object was created.
	 * @throws Exception If an error occurs
	 */
	public void launchServer() throws Exception {
		Server server = new Server(this.port);

		ServletContextHandler handler = new ServletContextHandler();
		handler.addServlet(new ServletHolder(new HomeServlet()), "/");
		handler.addServlet(new ServletHolder(new SearchServlet(this.invertedIndex)), "/search");

		server.setHandler(handler);

		server.start();
		System.err.printf("Server started at localhost:%d\n", this.port);
		server.join();
	}
}

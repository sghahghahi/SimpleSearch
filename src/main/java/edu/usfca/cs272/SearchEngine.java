package edu.usfca.cs272;

import java.nio.file.Path;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;


/** TODO */
public class SearchEngine {
	/** TODO */
	private final ThreadSafeInvertedIndex invertedIndex;

	/** TODO */
	private final int port;

	/** TODO */
	public static final Path template = Path.of("project-sghahghahi", "src", "main", "resources");

	/**
	 * TODO
	 * @param invertedIndex TODO
	 * @param port TODO
	 */
	public SearchEngine(ThreadSafeInvertedIndex invertedIndex, int port) {
		this.invertedIndex = invertedIndex;
		this.port = port;
	}

	/** TODO */
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

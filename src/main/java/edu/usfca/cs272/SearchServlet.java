package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet responsible for processing all form data from the user.
 *
 * @author Shyon Ghahghahi
 * @version Fall 2024
 */
public class SearchServlet extends HttpServlet {
	/** The inverted index to search through */
	private final ThreadSafeInvertedIndex invertedIndex;

	/**
	 * Constructs a {@code SearchServlet} object with a thread save inverted index to search through.
	 * @param invertedIndex The inverted index to search through
	 */
	public SearchServlet(ThreadSafeInvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String htmlTop = Files.readString(SearchEngine.template.resolve("searchTop.html"), UTF_8);
		String htmlBottom = Files.readString(SearchEngine.template.resolve("searchBottom.html"), UTF_8);

		String query = request.getParameter("q");
		query = query == null ? "" : StringEscapeUtils.escapeHtml4(query);

		List<InvertedIndex.SearchResult> searchResults = this.invertedIndex.partialSearch(FileStemmer.uniqueStems(query));
		StringBuilder resultHtml = new StringBuilder(htmlTop);

		for (var searchResult : searchResults) {
			resultHtml.append(String.format("<a href=\"%s\" class=\"list-group-item list-group-item-action\">%s</a>", searchResult.getLocation(), searchResult.getLocation()));
		}

		resultHtml.append(htmlBottom);

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(resultHtml.toString());
	}
}

package edu.usfca.cs272;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** TODO */
public class SearchServlet extends HttpServlet {
	/** TODO */
	private final ThreadSafeInvertedIndex invertedIndex;

	/**
	 * TODO
	 * @param invertedIndex TODO
	 */
	public SearchServlet(ThreadSafeInvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String query = request.getParameter("q");
		query = query == null ? "" : StringEscapeUtils.escapeHtml4(query);

		TreeSet<String> queryStems = FileStemmer.uniqueStems(query);
		List<InvertedIndex.SearchResult> searchResults = this.invertedIndex.partialSearch(queryStems);

		StringBuilder resultHtml = new StringBuilder();
		resultHtml.append("<h1>Search Results</h1>");

		for (var searchResult : searchResults) {
			resultHtml.append(String.format("<p><a href=\"%s\">%s</a></p>", searchResult.getLocation(), searchResult.getLocation()));
		}

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(resultHtml.toString());
	}
}

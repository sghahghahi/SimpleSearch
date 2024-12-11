package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
		String html = Files.readString(SearchEngine.template.resolve("searchTop.html"), UTF_8);

		String query = request.getParameter("q");
		query = query == null ? "" : StringEscapeUtils.escapeHtml4(query);

		List<InvertedIndex.SearchResult> searchResults = this.invertedIndex.partialSearch(FileStemmer.uniqueStems(query));
		StringBuilder resultHtml = new StringBuilder(html);

		for (var searchResult : searchResults) {
			resultHtml.append(String.format("<a href=\"%s\" class=\"list-group-item list-group-item-action\">%s</a>", searchResult.getLocation(), searchResult.getLocation()));
		}

		resultHtml.append(Files.readString(SearchEngine.template.resolve("searchBottom.html"), UTF_8));

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(resultHtml.toString());
	}
}

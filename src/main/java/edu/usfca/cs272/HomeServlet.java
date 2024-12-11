package edu.usfca.cs272;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet responsible for serving the home page.
 *
 * @author Shyon Ghahghahi
 * @version Fall 2024
 */
public class HomeServlet extends HttpServlet {
	/** Empty constructor is necessary to add this servlet to the server's handler. */
	public HomeServlet() {}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String html = Files.readString(SearchEngine.template.resolve("index.html"), UTF_8);

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(html);
	}
}

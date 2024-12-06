package edu.usfca.cs272;

import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Cleans simple, validating HTML 4/5 into plain text. For simplicity, this
 * class cleans already validating HTML, it does not validate the HTML itself.
 * For example, the {@link #stripEntities(String)} method removes HTML entities
 * but does not check that the removed entity was valid.
 *
 * <p>Look at the "See Also" section for useful classes and methods for
 * implementing this class.
 *
 * @see String#replaceAll(String, String)
 * @see Pattern#DOTALL
 * @see Pattern#CASE_INSENSITIVE
 * @see StringEscapeUtils#unescapeHtml4(String)
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class HtmlCleaner {
	/**
	 * Replaces all HTML tags with an empty string. For example, the html
	 * {@code A<b>B</b>C} will become {@code ABC}.
	 *
	 * <p><em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @param html valid HTML 4 text
	 * @return text without any HTML tags
	 *
	 * @see String#replaceAll(String, String)
	 */
	public static String stripTags(String html) {
		Pattern pattern = Pattern.compile("<[/!?]?[a-zA-Z][^>]*>", Pattern.DOTALL);

		return pattern.matcher(html).replaceAll("");
	}

	/**
	 * Replaces all HTML 4 entities with their Unicode character equivalent or, if
	 * unrecognized, replaces the entity code with an empty string. Should also work
	 * for entities that use decimal syntax like {@code &#8211;} for the &#8211;
	 * symbol or {@code &#x2013;} for the &#x2013; symbol.
	 *
	 * <p>For example, {@code 2010&ndash;2012} will become {@code 2010–2012} and
	 * {@code &gt;&dash;x} will become {@code >x} with the unrecognized
	 * {@code &dash;} entity getting removed. (The {@code &dash;} entity is valid
	 * HTML 5, but not valid HTML 4.)
	 *
	 * <p><em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @see StringEscapeUtils#unescapeHtml4(String)
	 * @see String#replaceAll(String, String)
	 *
	 * @param html valid HTML 4 text
	 * @return text with all HTML entities converted or removed
	 */
	public static String stripEntities(String html) {
		String htmlUnescaped = StringEscapeUtils.unescapeHtml4(html);

		return htmlUnescaped.replaceAll("&[^\\s;]+;", "");
	}

	/**
	 * Replaces all HTML comments with an empty string. For example:
	 *
	 * <pre>A&lt;!-- B --&gt;C</pre>
	 *
	 * ...and this HTML:
	 *
	 * <pre>
	 * A&lt;!--
	 * B --&gt;C</pre>
	 *
	 * ...will both become "AC" after stripping comments.
	 *
	 * <p><em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @param html valid HTML 4 text
	 * @return text without any HTML comments
	 *
	 * @see String#replaceAll(String, String)
	 */
	public static String stripComments(String html) {
		Pattern pattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

		return pattern.matcher(html).replaceAll("");
	}

	/**
	 * Replaces everything between the element tags and the element tags themselves
	 * with an empty string. For example, consider the html code:
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;
	 *   body { font-size: 10pt; }
	 * &lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed, and
	 * replaced with an empty string.
	 *
	 * <p><em>(View this comment as HTML in the Javadoc view.)</em>
	 *
	 * @param html valid HTML 4 text
	 * @param name name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 *
	 * @see String#formatted(Object...)
	 * @see String#format(String, Object...)
	 * @see String#replaceAll(String, String)
	 */
	public static String stripElement(String html, String name) {
		String regex = String.format("<%s\\b[^>]*>.*?</%s\\s*>", name, name);
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

		return pattern.matcher(html).replaceAll("");
	}

	/**
	 * A simple (but less efficient) approach for removing comments and certain
	 * block elements from the provided html. The block elements removed include:
	 * head, style, script, noscript, iframe, and svg.
	 *
	 * @param html valid HTML 4 text
	 * @return text clean of any comments and certain HTML block elements
	 */
	public static String stripBlockElements(String html) {
		html = stripComments(html);
		html = stripElement(html, "head");
		html = stripElement(html, "style");
		html = stripElement(html, "script");
		html = stripElement(html, "noscript");
		html = stripElement(html, "iframe");
		html = stripElement(html, "svg");
		return html;
	}

	/**
	 * Removes all HTML tags and certain block elements from the provided text.
	 *
	 * @see #stripBlockElements(String)
	 * @see #stripTags(String)
	 *
	 * @param html valid HTML 4 text
	 * @return text clean of any HTML tags and certain block elements
	 */
	public static String stripHtml(String html) {
		html = stripBlockElements(html);
		html = stripTags(html);
		html = stripEntities(html);
		return html;
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) { // TODO remove
		String html = """
				<!doctype html>
				<html lang="en">
				<head>
					<meta charset="utf-8">
					<title>Hello, world!</title>
				</head>
				<body>
					<style>
						body {
							font-size: 12pt;
						}
					</style>

					<p>Hello, <strong>world</strong>!</p>
					<p>&copy; 2023</p>
				</body>
				</html>
				""";

		/*
		 * The output should eventually look like:
		 *
		 * Hello, world!
		 * © 2023
		 */

		System.out.println("---------------------");
		System.out.println(html);
		System.out.println("---------------------");
		System.out.println(stripHtml(html));
		System.out.println("---------------------");
	}

	/** Prevent instantiating this class of static methods. */
	private HtmlCleaner() {
	}
}

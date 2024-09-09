package edu.usfca.cs272;

import java.nio.file.Path;
import java.io.IOException;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Shyon Ghahghahi
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		final String TEXT = "-text";
		final String COUNTS = "-counts";

		ArgumentParser argParser = new ArgumentParser(args);
		WordCounter wordCounter = new WordCounter();

		try {
			if (argParser.hasFlag(TEXT)) {
				if (argParser.getPath(TEXT) == null) {
					System.err.println("Missing input file.");
					return;
				}
				wordCounter.textFlag(argParser.getPath(TEXT));
			}

			if (argParser.hasFlag(COUNTS)) {
				wordCounter.countFlag(argParser.getPath(COUNTS, Path.of(".", "counts.json")));
			}

		} catch (IOException e) {
			System.err.println(e);
		}
	}
}

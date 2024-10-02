package edu.usfca.cs272;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2024
 */
public class ArgumentParser {
	/**
	 * Stores command-line arguments in flag/value pairs.
	 */
	private final HashMap<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentParser() {
		this.map = new HashMap<>();
	}

	/**
	 * Initializes this argument map and then parsers the arguments into flag/value
	 * pairs where possible. Some flags may not have associated values. If a flag is
	 * repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentParser(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a flag
	 * if it is a dash "-" character followed by any character that is not a digit
	 * or whitespace. For example, "-hello" and "-@world" are considered flags, but
	 * "-10" and "- hello" are not.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 */
	public static boolean isFlag(String arg) {
		final String PREFIX = "-";
		final int CHAR_AFTER_DASH = 1;

		if (
			arg == null ||
			arg.length() < 2 ||
			!arg.startsWith(PREFIX)
		) { return false; }

		// Safe to use `.codePointAt()` without fear of Exception because we are guaranteed at this point that `arg`'s length >= 2
		int second = arg.codePointAt(CHAR_AFTER_DASH);

		return !(Character.isDigit(second) || Character.isWhitespace(second));
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg) {
		return !isFlag(arg);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may not
	 * have associated values. If a flag is repeated, its value will be overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public final void parse(String[] args) {
		int lastFlagIndex = -1; // Initialize at -1 instead of 0 because index 0 is not guaranteed to be a flag
		String prevArg = null;	// Keeps track of the previous arg when iterating through `args`. Updates after each loop iteration

		// TODO this.map.isEmpty() check this before the loop
		
		for (int i = 0; i < args.length; i++) {
			String currArg = args[i];

			if (isFlag(currArg)) {
				this.map.put(currArg, null);
				lastFlagIndex = i;

			/*
			 * Here, we are at a value
			 * If the map is empty, we want to disregard the value (we have no flags in the map for this value to pair with)
			 * If the previous arg was also a value, ignore `currArg` since the only time we can add a value to the map is if a flag precedes it
			 */
			} else if (isValue(currArg)) {
				if (!(this.map.isEmpty() || isValue(prevArg))) { // TODO Remove the empty check here
					this.map.put(args[lastFlagIndex], currArg);
				}
			}

			prevArg = currArg;
		}
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags() {
		return this.map.size();
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag) {
		return this.map.containsKey(flag);
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag) {
		return this.map.get(flag) != null;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the backup value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param backup the backup value to return if there is no mapping
	 * @return the value to which the specified flag is mapped,
	 *   or the backup value if there is no mapping
	 */
	public String getString(String flag, String backup) {
		return Objects.requireNonNullElse(
			this.map.get(flag),
			backup
		);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *   there is no mapping
	 */
	public String getString(String flag) {
		return this.map.get(flag);
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *   backup value if there is no valid mapping
	 */
	public Path getPath(String flag, Path backup) {
		try { return Path.of(this.map.get(flag)); }
		catch (InvalidPathException | NullPointerException e) { return backup; }
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path}, or
	 * {@code null} if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *   unable to retrieve this mapping
	 */
	public Path getPath(String flag) {
		return getPath(flag, null);
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or the backup
	 * value if unable to retrieve this mapping (including being unable to convert
	 * the value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as an int, or the backup value
	 *   if there is no valid mapping
	 */
	public int getInteger(String flag, int backup) {
		try { return Integer.parseInt(this.map.get(flag)); }
		catch (NumberFormatException e) { return backup; }
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or 0 if
	 * unable to retrieve this mapping (including being unable to convert the value
	 * to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as an int, or 0 if there is no
	 *   valid mapping
	 */
	public int getInteger(String flag) {
		return getInteger(flag, 0);
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args the arguments to test
	 */
	public static void main(String[] args) { // TODO Remove
		// Feel free to modify or delete this method for debugging
		if (args.length < 1) {
			args = new String[] { "-max", "false", "-min", "0", "-min", "-10", "hello", "-@debug",
					"-f", "output.txt", "-verbose" };
		}

		// expected output:
		// {-max=false, -min=-10, -verbose=null, -f=output.txt, -@debug=null}
		ArgumentParser map = new ArgumentParser(args);
		System.out.println(map);
	}
}

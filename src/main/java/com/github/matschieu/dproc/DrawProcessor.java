package com.github.matschieu.dproc;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matschieu
 *
 */
public final class DrawProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DrawProcessor.class);

	private static final String CONF_FILE_NAME = "dproc.conf";
	private static final String COMMENTED_LINE_START = "#";
	private static final String VALUE_SEPARATOR = ",";

	private static final Random random = new SecureRandom();
	private static DrawProcessor instance = null;

	private List<String> draws = null;
	private boolean activate;

	/**
	 * Returns an instance of {@link DrawProcessor}.
	 * If the instance doesn't exist, it is created.
	 *
	 * @return {@link DrawProcessor}
	 */
	public static synchronized DrawProcessor build() {
		if (instance == null) {
			instance = new DrawProcessor();
		}
		return instance;
	}

	/**
	 * Returns an instance of {@link DrawProcessor}.
	 * If an instance of {@link DrawProcessor} exists, then it is destroyed and a new one is created.
	 *
	 * @return {@link DrawProcessor}
	 */
	public static synchronized DrawProcessor reload() {
		instance = null;
		return build();
	}

	/**
	 * Read a file to get its content as a list containing each line.
	 * If the file can't be read, then returning an empty list.
	 *
	 * @param filename
	 * @return {@link List}
	 */
	private List<String> readFile(String filename) {
		List<String> lines = new ArrayList<String>(0);
		URL url = DrawProcessor.class.getClassLoader().getResource(filename);

		if (url != null) {
			Path path = Paths.get(url.getPath());
			try {
				lines = Files.readAllLines(path).stream().filter(line -> !line.startsWith(COMMENTED_LINE_START)).toList();

				LOGGER.info("{} rows loaded from file {}", lines.size(), path);

				if (LOGGER.isTraceEnabled()) {
					lines.stream().forEach(line -> LOGGER.trace("{}", line));
				}
			} catch (InvalidPathException | IOException e) {
				LOGGER.warn("Error while loading file {}", path, e);
			}
		}

		return lines;
	}

	/**
	 * Convert a list of strings containing bytes comma separated into a list of printable strings.
	 * If the conversion is not possible, then returning null.
	 *
	 * @param lines
	 * @return {@link List}
	 */
	private List<String> decodeFileContent(List<String> lines) {
		List<String> decodedString = null;

		if (lines != null && !lines.isEmpty()) {
			decodedString = new ArrayList<String>(lines.size());

			for(final String line : lines) {
				final String[] stringBytes = line.split(VALUE_SEPARATOR);
				char[] buffer = new char[stringBytes.length];
				int idx = 0;

				for(final String value : stringBytes) {
					buffer[idx++] = (char)Byte.parseByte(value.trim());
				}

				decodedString.add(String.valueOf(buffer));
			}

		} else {
			decodedString = new ArrayList<String>(0);
			LOGGER.debug("Nothing to convert");
		}

		return decodedString;
	}

	/**
	 *
	 */
	private DrawProcessor() {
		final List<String> lines = this.readFile(CONF_FILE_NAME);
		this.draws = this.decodeFileContent(lines);
		this.activate();
	}

	/**
	 * Activate the draw processor
	 */
	public void activate() {
		this.activate = true;
	}

	/**
	 * Deactivate the draw processor
	 */
	public void deactivate() {
		this.activate = false;
	}

	/**
	 * Return whether the draw processor is activated
	 *
	 * @return {@link Boolean}
	 */
	public boolean isActive() {
		return this.activate;
	}

	/**
	 * Returns the number of draw available
	 *
	 * return int
	 */
	public int getNumberOfDraws() {
		return this.draws.size();
	}

	/**
	 * Returns the draw defined in the configuration file at the position defined by drawIndex.
	 *
	 * @param idx
	 * @return {@link String}
	 */
	public String getDraw(int idx) {
		return idx >= 0 && idx < this.draws.size() ? this.draws.get(idx) : null;
	}

	/**
	 * Display randomly one of the draws defined in the configuration file.
	 *
	 * @param printStream
	 */
	public void displayRandomly(PrintStream printStream) {
		int idx = random.nextInt(this.draws.isEmpty() ? 1 : this.draws.size());
		this.display(printStream, idx);
	}

	/**
	 * Display the draw defined in the configuration file at the position defined by idx.
	 *
	 * @param printStream
	 * @param idx
	 */
	public void display(PrintStream printStream, int idx) {
		if (isActive() && this.draws != null && this.draws.size() > 0) {
			LOGGER.debug("Displaying the draw at index {}", idx);

			final String draw = this.draws.get(idx);

			printStream.print(draw);
			printStream.flush();
		}
	}

}

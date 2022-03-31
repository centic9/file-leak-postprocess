package org.dstadler.filehandleleak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dstadler.commons.io.BufferedReaderWithPeek;

import com.google.common.annotations.VisibleForTesting;

/**
 * This class handles reported stacktraces from file-leak-detector.
 *
 * Method parse() will check if the line indicates the start of a stacktrace.
 * It will read all stacktrace-lines and return an object that can be used
 * for further processing.
 *
 * Lines listed in {@link org.dstadler.filehandleleak.FileHandleLeak#IGNORE_PATTERN_FILE}
 * are removed from stacktraces to make them as small as possible, while still retaining
 * all the required information for analysing and fixing the file-handle leaks.
 */
public class FileHandleLeak {
	public static final String IGNORE_PATTERN_FILE = "ignore_pattern.txt";

	// #228 /opt/cluster/bin/main/com/ebui/gwt/common/widgets/Search.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022
	private static final Pattern STACKTRACE_START = Pattern.compile("^#\\d+ (.*?) by thread:(.*) on (.*)");

	private static final List<Pattern> IGNORE_PATTERN = new ArrayList<>();

	static {
		// populate ignore-patterns from a text-file
		IGNORE_PATTERN.addAll(readIgnorePatterns(FileHandleLeak.class.getClassLoader().getResourceAsStream(IGNORE_PATTERN_FILE)));
	}

	@VisibleForTesting
	protected static List<Pattern> readIgnorePatterns(InputStream stream) {
		if (stream == null) {
			throw new IllegalStateException("Could not read file " + IGNORE_PATTERN_FILE + " from classpath");
		}

		List<Pattern> patterns = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}

				// ignore empty lines or lines starting with '#'
				if (StringUtils.isBlank(line) || line.startsWith("#")) {
					continue;
				}

				patterns.add(Pattern.compile(line));
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return patterns;
	}

	private final String header;
	private final String stacktrace;

	/**
	 * Check if this line is the start of a stacktrace.
	 *
	 * @param line   The current line
	 * @param reader The reader for fetching more lines if necessary
	 * @return A parsed FileHandleLeak or null if the given line is not the start of a file-handle-leak stacktrace
	 */
	public static FileHandleLeak parse(String line, BufferedReaderWithPeek reader) throws IOException {
		Matcher matcher = STACKTRACE_START.matcher(line);
		if (!matcher.matches()) {
			return null;
		}

		boolean hasMarkedIgnored = false;
		StringBuilder stackTrace = new StringBuilder();
		while (true) {
			String stack = reader.peekLine();
			if (stack == null) {
				// end of file, we may have a stacktrace
				return new FileHandleLeak(line, stackTrace.toString());
			}

			// the stacktrace continues as long as there is a tab at the beginning of the line
			if (!stack.startsWith("\t")) {
				return new FileHandleLeak(line, stackTrace.toString());
			}

			// we have to actually read the line now
			stack = reader.readLine();

			// check if this line should be removed from stacktraces
			if (isIgnored(stack)) {
				// add a ... for each removed block
				if (!hasMarkedIgnored) {
					stackTrace.append("\t...\n");
					hasMarkedIgnored = true;
				}
				continue;
			}

			stackTrace.append(stack).append('\n');

			// we wrote a normal line, so no marker is currently added
			hasMarkedIgnored = false;
		}
	}

	private static boolean isIgnored(String stack) {
		for (Pattern pattern : IGNORE_PATTERN) {
			if (pattern.matcher(stack).matches()) {
				return true;
			}
		}

		return false;
	}

	public FileHandleLeak(String header, String stacktrace) {
		this.header = header;
		this.stacktrace = stacktrace;
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public String getHeader() {
		return header;
	}
}

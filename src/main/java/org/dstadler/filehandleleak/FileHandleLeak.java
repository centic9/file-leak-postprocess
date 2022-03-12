package org.dstadler.filehandleleak;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHandleLeak {

	// #228 /opt/cluster/bin/main/com/ebui/gwt/common/widgets/Search.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022
	private static final Pattern STACKTRACE_START = Pattern.compile("^#\\d+ (.*?) by thread:(.*) on (.*)");

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

			stackTrace.append(stack).append('\n');
		}
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

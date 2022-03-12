package org.dstadler.filehandleleak;

import java.io.BufferedReader;
import java.io.IOException;

public class BufferedReaderWithPeek implements AutoCloseable {

	private final BufferedReader delegate;

	private String peekedLine = null;

	public BufferedReaderWithPeek(BufferedReader delegate) {
		this.delegate = delegate;
	}

	/**
	 * Return the next line without taking it off of the BufferedReader
	 * <p>
	 * Calling this method multiple times without "readLine()" returns
	 * the same line.
	 *
	 * @return The line that would be returned by readLine(). Multiple calls will return the same line.
	 * Only calling readLine() will advance the reader.
	 * @throws java.io.IOException If reading from the reader fails
	 */
	public String peekLine() throws IOException {
		if (peekedLine == null) {
			peekedLine = delegate.readLine();
		}

		return peekedLine;
	}

	/**
	 * Returns the next line from the BufferedReader, may be the line
	 * that was previously returned via #peekLine().
	 *
	 * @see java.io.BufferedReader#readLine() for details
	 */
	public String readLine() throws IOException {
		if (peekedLine != null) {
			String line = peekedLine;
			peekedLine = null;
			return line;
		}

		return delegate.readLine();
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}
}

package org.dstadler.filehandleleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.NullReader;
import org.apache.commons.lang3.StringUtils;
import org.dstadler.commons.io.BufferedReaderWithPeek;
import org.junit.jupiter.api.Test;

class FileHandleLeakTest {
	@Test
	public void testConstruct() {
		FileHandleLeak leak = new FileHandleLeak("header", "stacktrace");
		assertEquals("header", leak.header());
		assertEquals("stacktrace", leak.stacktrace());
		assertEquals("stacktrace", leak.getLastLine());
	}

	@Test
	public void testParseLineNoMatch() throws IOException {
		assertNull(FileHandleLeak.parse("header", null));
		assertNull(FileHandleLeak.parse("some longer line with no stacktrace", null));
		assertNull(FileHandleLeak.parse(" #227 /opt/OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022", null));
	}

	@Test
	public void testParseLineEmptyStack() throws IOException {
		FileHandleLeak leak = FileHandleLeak.parse(
				"#227 /opt/OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new NullReader())));

		assertNotNull(leak);
		assertTrue(StringUtils.isNotBlank(leak.header()));
		assertEquals("", leak.stacktrace());
		assertEquals("", leak.getLastLine());
	}

	@Test
	public void testParseLineAndStack() throws IOException {
		FileHandleLeak leak = FileHandleLeak.parse(
				"#227 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"""
								\tstack1
								\tstack2
								no stack any more"""))));

		assertNotNull(leak);
		assertTrue(StringUtils.isNotBlank(leak.header()));
		assertEquals("\tstack1\n\tstack2\n", leak.stacktrace());
		assertEquals("\tstack2", leak.getLastLine());
	}

	@Test
	public void testParseLineAndStackWithSpaces() throws IOException {
		FileHandleLeak leak = FileHandleLeak.parse(
				"#227 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"""
								        stack1
								        stack2
								no stack any more"""))));

		assertNotNull(leak);
		assertTrue(StringUtils.isNotBlank(leak.header()));
		assertEquals("        stack1\n        stack2\n", leak.stacktrace());
		assertEquals("        stack2", leak.getLastLine());
	}

	@Test
	public void testParseSampleFile() throws IOException {
		int count = 0;
		try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek("src/test/resources/output.txt")) {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}

				FileHandleLeak leak = FileHandleLeak.parse(line, reader);
				if (leak != null) {
					count++;

					assertNotNull(leak.header());
					String stacktrace = leak.stacktrace();
					assertNotNull(stacktrace);
					assertNotNull(leak.getLastLine());

					assertFalse(leak.getLastLine().contains("..."),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );

					// we want to replace all lines for some packages
					assertFalse(stacktrace.contains("junit"),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );
					assertFalse(stacktrace.contains("mockito"),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );
					assertFalse(stacktrace.contains("gradle"),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );
					assertFalse(stacktrace.contains("java.util"),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );
					assertFalse(stacktrace.contains("java.lang"),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );
					assertFalse(stacktrace.contains("akka.dispatch.forkjoin"),
							"Had: " + leak.header() + "\n"  + stacktrace + "\n\n" +
									leak.getLastLine() );
				}
			}
		}

		assertEquals(3, count);
	}

	@Test
	public void testIgnoreLines() throws IOException {
		FileHandleLeak leak = FileHandleLeak.parse(
				"#227 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"""
								\tstack1
								\tat java.base/java.util.stream.blabla1
								\tat java.base/java.util.stream.blabla2
								\tat java.base/java.util.stream.blabla3
								\tstack2
								\tat java.base/java.util.stream.blabla4
								no stack any more"""))));

		assertNotNull(leak);
		assertTrue(StringUtils.isNotBlank(leak.header()));
		assertEquals("\tstack1\n\t...\n\tstack2\n\t...\n", leak.stacktrace());
		assertEquals("\tstack2", leak.getLastLine());
	}

	@Test
	public void testReadIgnorePatternsNotFound() {
		assertThrows(IllegalStateException.class, () -> FileHandleLeak.readIgnorePatterns(null));
	}

	@Test
	public void testReadIgnorePatternsException() {
		assertThrows(IllegalStateException.class,
				() -> FileHandleLeak.readIgnorePatterns(new NullInputStream(100, false, true)));
	}

	@Test
	public void testReadIgnorePatterns() {
		assertEquals(0, FileHandleLeak.readIgnorePatterns(new NullInputStream()).size());
		assertEquals(0, FileHandleLeak.readIgnorePatterns(
				new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))).size());

		assertEquals(2, FileHandleLeak.readIgnorePatterns(
				new ByteArrayInputStream((
						"""
								pattern1
								pattern2
								
								
								# a comment
								
								""").getBytes(StandardCharsets.UTF_8))).size());
	}
}
package org.dstadler.filehandleleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

class BufferedReaderWithPeekTest {

	@Test
	public void testReader() throws IOException {
		try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek("src/test/resources/output.txt")) {
			assertEquals("openjdk version \"11.0.14\" 2022-01-18", reader.peekLine());
			assertEquals("openjdk version \"11.0.14\" 2022-01-18", reader.peekLine());

			assertEquals("openjdk version \"11.0.14\" 2022-01-18", reader.readLine());
			assertEquals("OpenJDK Runtime Environment (build 11.0.14+9-Ubuntu-0ubuntu2.20.04)", reader.readLine());

			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
			}

			assertNull(reader.peekLine());
			assertNull(reader.readLine());
		}
	}

	@Test
	public void testReaderConstructor() throws IOException {
		try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek(new BufferedReader(new StringReader("test\ntest2")))) {
			assertEquals("test", reader.peekLine());
			assertEquals("test", reader.peekLine());

			assertEquals("test", reader.readLine());
			assertEquals("test2", reader.readLine());

			assertNull(reader.peekLine());
			assertNull(reader.readLine());
		}
	}

	@Test
	public void testReaderInvalidFile() {
		assertThrows(FileNotFoundException.class, () -> new BufferedReaderWithPeek("invalid-file"));
	}
}
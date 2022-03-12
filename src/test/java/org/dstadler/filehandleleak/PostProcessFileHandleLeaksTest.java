package org.dstadler.filehandleleak;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PostProcessFileHandleLeaksTest {
	@Test
	public void testReadFileHandleLeaks() throws IOException {
		List<FileHandleLeak> leaks = PostProcessFileHandleLeaks.readFileHandleLeaks(new String[] {
				"src/test/resources/output.txt"
		});

		assertEquals(2, leaks.size());
	}

	@Test
	public void testProcessFileHandleLeaks() {
		List<FileHandleLeak> leaks = new ArrayList<>();

		// nothing much happens on empty list
		PostProcessFileHandleLeaks.processFileHandleLeaks(leaks);

		leaks.add(new FileHandleLeak("header", "stacktrace"));

		// this mostly produces output to stderr and stdout, we do not specifically test for this for now...
		PostProcessFileHandleLeaks.processFileHandleLeaks(leaks);
	}

	@Test
	public void testMain() throws Exception {
		PostProcessFileHandleLeaks.main(new String[] {
				"src/test/resources/output.txt"
		});
	}

	@Test
	public void testMainInvalidArg() {
		assertThrows(FileNotFoundException.class,
				() -> PostProcessFileHandleLeaks.main(new String[] {
				"invalid-file.txt"
		}));
	}

	@Test
	public void testMainNoArgs() throws Exception {
		PostProcessFileHandleLeaks.main(new String[0]);
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(PostProcessFileHandleLeaks.class);
	}
}
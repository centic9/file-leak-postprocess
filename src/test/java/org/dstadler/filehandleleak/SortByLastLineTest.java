package org.dstadler.filehandleleak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;

import org.dstadler.commons.io.BufferedReaderWithPeek;
import org.junit.jupiter.api.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class SortByLastLineTest {
	@Test
	public void testCompare() throws IOException {
		Multimap<String, FileHandleLeak> leaksByStacktrace = HashMultimap.create();

		FileHandleLeak leak1 = FileHandleLeak.parse(
				"#226 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"\tstack1\n" +
								"\tstack2\n" +
								"no stack any more"))));
		assertNotNull(leak1);
		leaksByStacktrace.put(leak1.getHeader(), leak1);

		FileHandleLeak leak2 = FileHandleLeak.parse(
				"#226 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"\tstack1\n" +
								"\tstack2\n" +
								"no stack any more"))));
		assertNotNull(leak2);
		leaksByStacktrace.put(leak2.getHeader(), leak2);

		FileHandleLeak leak3 = FileHandleLeak.parse(
				"#228 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"\tstack1\n" +
								"\tstack3\n" +
								"no stack any more"))));
		assertNotNull(leak3);
		leaksByStacktrace.put(leak3.getHeader(), leak3);

		FileHandleLeak leak4 = FileHandleLeak.parse(
				"#229 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"\tstack1\n" +
								"\tstack2\n" +
								"no stack any more"))));
		assertNotNull(leak4);
		leaksByStacktrace.put(leak4.getHeader(), leak4);

		FileHandleLeak leak5 = FileHandleLeak.parse(
				"#230 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"\tstack1\n" +
								"\tstack2\n" +
								"other stack any more"))));
		assertNotNull(leak5);
		leaksByStacktrace.put(leak5.getHeader(), leak5);

		Comparator<String> comp = new SortByLastLine(leaksByStacktrace);

		assertEquals(0, comp.compare(leak1.getHeader(), leak2.getHeader()));
		assertTrue(comp.compare(leak1.getHeader(), leak3.getHeader()) < 0,
				"Had: " + comp.compare(leak1.getHeader(), leak3.getHeader()));
		assertTrue(comp.compare(leak3.getHeader(), leak1.getHeader()) > 0,
				"Had: " + comp.compare(leak3.getHeader(), leak1.getHeader()));
		assertTrue(comp.compare(leak4.getHeader(), leak5.getHeader()) < 0,
				"Had: " + comp.compare(leak4.getHeader(), leak5.getHeader()));
	}

	@Test
	public void testCompareInvalid() throws IOException {
		Multimap<String, FileHandleLeak> leaksByStacktrace = HashMultimap.create();
		Comparator<String> comp = new SortByLastLine(leaksByStacktrace);

		FileHandleLeak leak = FileHandleLeak.parse(
				"#226 /opt//OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
						"\tstack1\n" +
								"\tstack2\n" +
								"no stack any more"))));
		assertNotNull(leak);
		leaksByStacktrace.put(leak.getHeader(), leak);

		//noinspection ResultOfMethodCallIgnored
		assertThrows(IllegalStateException.class,
				() -> comp.compare(leak.getHeader(), "blabla"));
		//noinspection ResultOfMethodCallIgnored
		assertThrows(IllegalStateException.class,
				() -> comp.compare("blabla2", leak.getHeader()));
	}
}
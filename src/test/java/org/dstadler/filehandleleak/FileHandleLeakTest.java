package org.dstadler.filehandleleak;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.input.NullReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class FileHandleLeakTest {
	@Test
	public void testConstruct() {
		FileHandleLeak leak = new FileHandleLeak("header", "stacktrace");
		assertEquals("header", leak.getHeader());
		assertEquals("stacktrace", leak.getStacktrace());
	}

	@Test
	public void testParseLineNoMatch() throws IOException {
		assertNull(FileHandleLeak.parse("header", null));
		assertNull(FileHandleLeak.parse("some longer line with no stacktrace", null));
		assertNull(FileHandleLeak.parse(" #227 /opt/cluster/managed/ui/components/gwt.common.onprem/bin/main/com/example/test/webui/gwt/common/onprem/widgets/testbutton/OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022", null));
	}

	@Test
	public void testParseLineEmptyStack() throws IOException {
		FileHandleLeak leak = FileHandleLeak.parse(
				"#227 /opt/cluster/managed/ui/components/gwt.common.onprem/bin/main/com/example/test/webui/gwt/common/onprem/widgets/testbutton/OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new NullReader())));

		assertNotNull(leak);
		assertTrue(StringUtils.isNotBlank(leak.getHeader()));
		assertEquals("", leak.getStacktrace());
	}

	@Test
	public void testParseLineAndStack() throws IOException {
		FileHandleLeak leak = FileHandleLeak.parse(
				"#227 /opt/cluster/managed/ui/components/gwt.common.onprem/bin/main/com/example/test/webui/gwt/common/onprem/widgets/testbutton/OnPremTestButtonWidget.ui.xml by thread:Test worker on Sat Mar 12 07:55:16 CET 2022",
				new BufferedReaderWithPeek(new BufferedReader(new StringReader(
					"\tstack1\n" +
						"\tstack2\n" +
						"no stack any more"))));

		assertNotNull(leak);
		assertTrue(StringUtils.isNotBlank(leak.getHeader()));
		assertEquals("\tstack1\n\tstack2\n", leak.getStacktrace());
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
				}
			}
		}

		assertEquals(2, count);
	}
}
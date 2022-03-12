package org.dstadler.filehandleleak;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class PostProcessFileHandleLeaks {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            System.err.println("Usage: " + PostProcessFileHandleLeaks.class.getName() + " <text-file> [<test-file> ...]");
            return;
        }

        // walk all files and collect all found stack-traces
		List<FileHandleLeak> leaks = new ArrayList<>();
        for(String location : args) {
            System.err.println("Handling file " + location);

			try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek(location)) {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}

					FileHandleLeak leak = FileHandleLeak.parse(line, reader);
					if (leak != null) {
						leaks.add(leak);
					}
				}
			}
        }

		System.err.println("Found " + leaks.size() + " file-handle-leaks in " + ArrayUtils.toString(args));
    }

}

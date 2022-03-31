package org.dstadler.filehandleleak;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dstadler.commons.arrays.ArrayUtils;
import org.dstadler.commons.collections.MappedCounter;
import org.dstadler.commons.collections.MappedCounterImpl;
import org.dstadler.commons.io.BufferedReaderWithPeek;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class PostProcessFileHandleLeaks {
    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
			System.err.println("Specify one or more text-files with stacktraces produced by file-leak-detector");
			System.err.println("\nCombined output will be printed to stdout\n");
            System.err.println("Usage: " + PostProcessFileHandleLeaks.class.getName() + " <text-file> [<test-file> ...]");
            return;
        }

        // walk all files and collect all found stack-traces
		List<FileHandleLeak> leaks = readFileHandleLeaks(args);

		System.err.println("Found " + leaks.size() + " file-handle-leaks in " +
				ArrayUtils.toString(args, ", ", "", ""));

		// print out de-duplicated stacktraces
		processFileHandleLeaks(leaks);
	}

	protected static List<FileHandleLeak> readFileHandleLeaks(String[] args) throws IOException {
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
		return leaks;
	}

	protected static void processFileHandleLeaks(List<FileHandleLeak> leaks) {
		Multimap<String, FileHandleLeak> leaksByStacktrace = HashMultimap.create();
		MappedCounter<String> uniqueStacks = new MappedCounterImpl<>();
		for (FileHandleLeak leak : leaks) {
			uniqueStacks.inc(leak.getStacktrace());
			leaksByStacktrace.put(leak.getStacktrace(), leak);
		}

		// print an overview to stderr
		System.err.println("Had " + uniqueStacks.sortedMap().keySet().size() + " unique stacktraces: " + uniqueStacks.sortedMap().values());

		// print combined stacktraces to stdout
		for (String stackTrace : leaksByStacktrace.keySet()) {
			// print all headers
			for (FileHandleLeak fileHandleLeak : leaksByStacktrace.get(stackTrace)) {
				System.out.println(fileHandleLeak.getHeader());
			}

			System.out.println(stackTrace);
		}
	}
}

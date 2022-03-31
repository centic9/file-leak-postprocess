package org.dstadler.filehandleleak;

import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

/**
 * A comparator which sorts {@link org.dstadler.filehandleleak.FileHandleLeak} instances
 * first by the "last line" of the Stacktrace and then by header-line.
 *
 * This is used to group stacktraces by the location and is mostly useful for tests to
 * be able to investigate test-by-test
 */
public class SortByLastLine implements Comparator<String> {
	private final Multimap<String, FileHandleLeak> leaksByStacktrace;

	public SortByLastLine(Multimap<String, FileHandleLeak> leaksByStacktrace) {
		this.leaksByStacktrace = leaksByStacktrace;
	}

	@Override
	public int compare(String o1, String o2) {
		// equal header is equal as this is the key in the map
		if (StringUtils.equals(o1, o2)) {
			return 0;
		}

		Collection<FileHandleLeak> leak1 = leaksByStacktrace.get(o1);
		Preconditions.checkState(!leak1.isEmpty(), "Had no entries for %s", o1);

		Collection<FileHandleLeak> leak2 = leaksByStacktrace.get(o2);
		Preconditions.checkState(!leak2.isEmpty(), "Had no entries for %s", o2);

		// for not equal, sort by "last stacktrace"
		String lastLine1 = leak1.iterator().next().getLastLine();
		String lastLine2 = leak2.iterator().next().getLastLine();
		int ret = lastLine1.compareTo(lastLine2);
		if (ret != 0) {
			return ret;
		}

		// if last stacktrace is equal, sort by header-line
		return o1.compareTo(o2);
	}
}

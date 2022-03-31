#!/bin/bash

set -eu

OUTFILE=/tmp/file-handle-leaks.txt
TOOLDIR=`dirname $0`

if [ $# -lt 1 ]; then
  echo Specify one or more text-files with stacktraces produced by file-leak-detector
  echo
  echo Output will be stored in ${OUTFILE}
  echo
  echo "file-leak-postprocess <text-file> [<text-file> ...]"
  echo

  exit 1
fi

# run file-leak-postprocess with the given files
(cd "${TOOLDIR}" && ./gradlew check installDist)

echo
"${TOOLDIR}/build/install/file-leak-postprocess/bin/file-leak-postprocess" "$@" > ${OUTFILE}

echo
echo Written combined stacktraces to ${OUTFILE}
ls -al ${OUTFILE}

exit 0

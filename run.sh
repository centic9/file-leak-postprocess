#!/bin/bash

set -eu

if [ $# -lt 1 ]; then
  echo Specify one or more text-files with stacktraces produced by file-leak-detector
  echo
  echo "file-leak-postprocess <text-file> [<text-file> ...]"
  echo

  exit 1
fi

# run file-leak-postprocess with the given files
./gradlew check installDist

echo
build/install/file-leak-postprocess/bin/file-leak-postprocess "$@" > /tmp/file-handle-leaks.txt

echo
echo Written combined stacktraces to /tmp/file-handle-leaks.txt
ls -al /tmp/file-handle-leaks.txt

exit 0

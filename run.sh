#!/bin/bash

set -eu

# run file-leak-postprocess with the given files
./gradlew check installDist

echo
build/install/file-leak-postprocess/bin/file-leak-postprocess "$@" > /tmp/file-handle-leaks.txt

echo
echo Written combined stacktraces to /tmp/file-handle-leaks.txt
ls -al /tmp/file-handle-leaks.txt

exit 0

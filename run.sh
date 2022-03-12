#!/bin/bash

set -eu

# run file-leak-postprocess with the given files
./gradlew check installDist && build/install/file-leak-postprocess/bin/file-leak-postprocess "$@" > /tmp/file-handle-leaks.txt

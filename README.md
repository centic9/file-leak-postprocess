[![Gradle Status](https://gradleupdate.appspot.com/centic9/file-leak-postprocess/status.svg?branch=master)](https://gradleupdate.appspot.com/centic9/file-leak-postprocess/status)

This is a small tool to post-process output from running an application with [file-leak-detector](https://github.com/jenkinsci/lib-file-leak-detector).

file-leak-detector will print out all found stacktraces where files were not closed. 

This has a few shortcomings when running on large-scale projects.

* Multiple equal stacktrace are printed, requiring to skim through many similar results
* Many items in the stacktraces are unimportant for analyzing the file-handle leaks, e.g. thread-pools, JUnit calls, ...

So this tool parses one or more input text-files with file-leak-detector stacktraces and will shorten 
and de-duplicate them into a smaller list of actual code-locations.

The found stack-traces are printed to stdout. Summary/Error information is printed to stderr.

So a typical invocation will redirect stdout to a file via `> file-handle-leaks.txt`

#### Getting started

##### Grab it

    git clone git://github.com/centic9/file-leak-postprocess
    cd file-leak-postprocess

##### Build it

    ./gradlew check installDist

#### Run it

    build/install/file-leak-postprocess/bin/file-leak-postprocess <text-file> > file-handle-leaks.txt

### How it works

The actual code is quite small, it parses the text-files and parses all found stacktraces.

Then it shortens the stacks and then de-duplicates them for output.

### Support this project

If you find this tool useful and would like to support it, you can [Sponsor the author](https://github.com/sponsors/centic9)

### Licensing

   Copyright 2022 Dominik Stadler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

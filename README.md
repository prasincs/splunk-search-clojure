# splunksearch

A Clojure command line program that enables simple search access to a
splunk utility.

## Usage

This is a bit tricky because this program relies on both the Splunk
Java SDK, which is not in Clojure or Maven repos, and the Splunk Java
SDK's Command utility class.  You'll have to download the Splunk Java
SDK yourself and install the Splunk JAR file.  You'll also have to
create, from the root of this project directory, resources/com/splunk,
and deposit Command.class (which can be found in the SDK's tree
somewhere) in the newly created directory.

I used lein localrepo to install the Splunk JAR file.  It seems to
have worked for me.

Once all that's done, *and* you've got an instance of Splunk up at
running, *and* you've successfully configure your .splunkrc file, you
can try:

lein run 'search <your search here>'

## License

Copyright © 2013 Elf M. Sternberg

Distributed under the Apache Public License, under the same terms as
other Splunk software.

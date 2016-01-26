#!/bin/bash
# run client
CP="dist/core.jar:dist/client.jar:.:lib/log4j-1.2.16.jar"
MAIN="wagyx001.pa1.Client"
DEFS="-Djava.rmi.server.codebase=http://localhost/classes/client.jar -Djava.rmi.server.hostname=localhost"
java -cp $CP $DEFS $MAIN

#!/bin/bash
# run client
CP=".:dist/core.jar:dist/node.jar:lib/log4j-1.2.16.jar"
MAIN="wagyx001.pa1.Node"
DEFS="-Djava.rmi.server.codebase=http://localhost/classes/core.jar -Djava.rmi.server.hostname=localhost"
java -cp $CP $DEFS $MAIN

#!/bin/sh
LANG=en_US.utf-8

ConfigPath=$(cd "$(dirname "$0")"; cd ..;pwd)
rootPath=$(cd ..;pwd)

CLASS_PATH="$ConfigPath":"$JAVA_HOME"/lib/tools.jar

for jar in "$ConfigPath"/lib/*.jar; do
  CLASS_PATH=$CLASS_PATH:$jar
done

java  -classpath $CLASS_PATH -DrootPath=$rootPath -Xms1024M -Xmx2048M com.hivin.tools.Application


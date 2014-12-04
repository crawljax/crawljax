#!/bin/sh
# Get the current dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Go to crawljax main folder
cd $DIR/../../../../

# Install the latest version locally
mvn clean install -DskipTests=true

#Use the target folder as a tmp folder
cd $DIR/../../../target

# Create the installed archtype with name 'testname'
mvn archetype:generate -B \
  -DarchetypeGroupId=com.crawljax.plugins.archetypes \
  -DarchetypeArtifactId=crawljax-plugins-archetype \
  -DarchetypeVersion=4.0-SNAPSHOT \
  -DgroupId=test \
  -DartifactId=testname \
  -Dversion=1.0-SNAPSHOT

# Open the generated project and compile
cd testname
mvn compile
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR/../../../../
mvn clean install -DskipTests=true
cd $DIR/../../../target

mvn archetype:generate -B \
  -DarchetypeGroupId=com.crawljax.plugins.archetypes \
  -DarchetypeArtifactId=crawljax-plugins-archetype \
  -DarchetypeVersion=3.1-SNAPSHOT \
  -DgroupId=test \
  -DartifactId=testname \
  -Dversion=1.0-SNAPSHOT

cd testname
mvn compile



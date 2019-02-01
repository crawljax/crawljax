#! /bin/bash

IMAGE="crawljax-docker-test"

echo "Building Docker image tagged as '$IMAGE'..."
## only builds again if things (e.g., pom.xml files) have changed,
## otherwise, uses the already built image.
docker build  -t $IMAGE .

echo "Running mvn install on '$IMAGE' container..."
docker run -it -v "$PWD:/work" -w "/work" $IMAGE mvn -s /usr/share/maven/ref/settings-docker.xml clean install

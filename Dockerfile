FROM maven:3.5.2-jdk-8 AS MAVEN_TOOL_CHAIN

USER root

# install Chrome
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
	&& echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
	&& apt-get update -qqy \
	&& apt-get -qqy install google-chrome-stable \
	&& rm /etc/apt/sources.list.d/google-chrome.list \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
	&& sed -i 's/"$HERE\/chrome"/"$HERE\/chrome" --no-sandbox/g' /opt/google/chrome/google-chrome

# install Firefox
RUN apt-get update \
	&& apt-get purge firefox \
	&& apt-cache showpkg firefox \
	&& apt-get install -y wget libfreetype6 libfontconfig1 libxrender1 libasound-dev libdbus-glib-1-dev libgtk2.0-0 libxt6 python-pip \
	&& wget -O firefox.tar.bz2 https://download.mozilla.org/?product=firefox-latest-ssl\&os=linux64\&lang=en-US \
	&& tar -xjf firefox.tar.bz2 \
	&& mv firefox /opt/firefox \
	&& ln -s /opt/firefox/firefox /usr/bin/firefox \
	&& ls /opt/firefox \ 
	&& firefox --version

# cache Maven dependencies
ADD pom.xml /cj/
ADD core/pom.xml /cj/core/
ADD examples/pom.xml /cj/examples/
ADD plugins/pom.xml /cj/plugins/
ADD plugins/crawloverview-plugin/pom.xml /cj/plugins/crawloverview-plugin/
ADD plugins/test-plugin/pom.xml /cj/plugins/test-plugin/
ADD plugins/testcasegenerator-plugin/pom.xml /cj/plugins/testcasegenerator-plugin/
ADD cli/pom.xml /cj/cli/pom.xml
ADD cli/src/main/resources/jar-with-dependencies.xml cj/cli/src/main/resources/jar-with-dependencies.xml
WORKDIR /cj/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml dependency:resolve-plugins clean package -DskipTests -Dcheckstyle.skip -Dasciidoctor.skip -Djacoco.skip -Dmaven.gitcommitid.skip -Dspring-boot.repackage.skip -Dmaven.exec.skip=true -Dmaven.install.skip -Dmaven.resources.skip

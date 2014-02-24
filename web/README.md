#Crawljax web interface

This is the web distribution of Crawljax. This web application can be used to configure and run Crawljax from a browser.

## Distribution

When you run 

	mvn clean package
	
The result will be available as a distribution zip and directory in the target folder. The distribution has the following layout:

- *lib* contains the jars that are needed to run
- *conf* contains any configuration files like `logback.xml`.
- *plugins* can contain any jars that should be loaded as a plug-in.
- *crawljax-web-{version}-.jar* is the actual program.

The `conf` and `plugins` folders are simply added to the class path.

## Running
Unzip the distribution zip and in the resulting folder you can run Crawljax Web as follows:

```
usage: java -jar crawljax-web-{version}.jar
	-p,--port <arg>     	Port number. Default is 8080
    -o,--outputDir <arg>    Output directory. Default is /out
```

You can then browse to `http://localhost:{port}/` to begin using Crawljax.

## Implementation
Based on the common java web application stack. [See tutorial here](http://blog.palominolabs.com/2011/08/15/a-simple-java-web-stack-with-guice-jetty-jersey-and-jackson/)

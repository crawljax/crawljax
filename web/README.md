#Crawljax web interface

Based on the common java web application stack. [See tutorial here](http://blog.palominolabs.com/2011/08/15/a-simple-java-web-stack-with-guice-jetty-jersey-and-jackson/)

## Running
Run the web application using the main method in `CrawljaxServer`. This will start the webapplication at [http://localhost:8080]();

## Distribution

When you run 

	mvm clean package
	
The result will be available as a distribution zip and directory in the target folder. The distribution has the following layout:

- *lib* contains the jars that are needed to run
- *conf* contains any configuration files like `logback.xml`.
- *plugins* can contain any jars that should be loaded as a plug-in.
- *crawljax-web-{version}-.jar* is the actual program.

You can run the program by running

	java -jar crawljax-web-{version}-.jar
	
The `conf` and `plugins` folders are simply added to the class path.

## Defaults
The CrawlOverView plugin is automatically added to the plugins folder. When running in Eclipse, it is simply present in the class path via Maven.
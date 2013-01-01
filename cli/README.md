# Crawljax Command-line
This is the Command-line distribution of Crawljax. The project is assembled in a ZIP file containing the jar that you can run to execute the crawler.

You can assemble the zip using

	mvn assembly:assembly
	
If you unzip the Jar you can run the crawler using

	java -jar crawljax-cli-x.y-SNAPSHOT.jar http://yoursite.com
	
This will run the crawler on the specified site.
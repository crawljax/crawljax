# Crawljax Command-line
This is the Command-line distribution of Crawljax. The project is assembled in a ZIP file containing the jar that you can run to execute the crawler.

	
Unzip the zip and in the resulting folder you can run Crawljax as follows:

	usage: java -jar crawljax-cli-VERSION.jar -url=http://google.com
	  OPTIONS:
	   --browser browser type: firefox, chrome, ie, htmlunit
	   --depth crawl depth level
	  -help print this message
	   --maxstates max number of states to crawl
	  -url url to crawl
	  -version print the version information and exit
	
``-url`` is a required argument.

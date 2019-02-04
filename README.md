Crawljax 
========

Crawljax is a tool for automatically crawling and testing modern web applications. 
Crawljax can explore any JavaScript-based Ajax web application through an event-driven dynamic crawling engine.
It produces as output a state-flow graph of the dynamic DOM states and the event-based transitions between them.
Crawljax can easily be extended through its easy-to-use [plugin architecture](https://github.com/crawljax/crawljax/wiki/Writing-a-plugin).

For more project info visit the [Crawljax website](http://crawljax.com).

Maven
-----

	<dependency>
	    <groupId>com.crawljax</groupId>
	    <artifactId>crawljax-core</artifactId>
	    <version>4.1</version>
	</dependency>

Documentation
-------------

You can find more technical documentation in our [project wiki](https://github.com/crawljax/crawljax/wiki/). 


Community
---------

Keep track of development and community news.

* Follow [@crawljax](https://twitter.com/crawljax) on Twitter.
* Read the Crawljax [Blog](http://crawljax.com/).
* Need a new feature or bug report? [Use the issue tracker](https://github.com/crawljax/crawljax/issues).
* Have a question that's not a feature request or bug report? Ask on the [mailing list](https://groups.google.com/group/crawljax).


Changelog
---------

Detailed change history is avaialbe in our [changelog](https://github.com/crawljax/crawljax/blob/master/CHANGELOG.md).

Testing inside a Docker container
---------------------------------
	
Install [Docker](https://www.docker.com/) and run the "run-tests-in-docker.sh" script. It's recommended to increse your Docker memory to at least 4.0 GiB to avoid OOMs during testing. 

Testing on your machine
-----------------------

You can run the tests using maven with the command

	mvn clean package
	
If you want to run all the test, including browser tests, run 

	mvn clean test -P integrationtests

The default driver used for testing uses Chrome so make sure you have Chrome installed on your machine. You can also specify which browser you'd like to use with the `test.browser` variable. For example, if you want to test with Firefox, use:

	mvn clean test -P integrationtests -Dtest.browser=FIREFOX

Again, make sure a recent version of Firefox is installed on your machine before running the tests.	

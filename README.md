Crawljax 
========

Crawljax is a tool for automatically crawling and testing modern web applications. 
Crawljax can explore any (even single-page dynamic JavaScript-based) web application through an event-driven dynamic crawling engine.
It produces as output a state-flow graph of the dynamic DOM states and the event-based transitions between them.
Crawljax can easily be extended through its easy-to-use [plugin architecture](https://github.com/crawljax/crawljax/wiki/Writing-a-plugin).

Maven
-----
Crawljax artifacts are available on [Maven central repository](https://repo.maven.apache.org/maven2/com/crawljax/).

	<dependency>
	    <groupId>com.crawljax</groupId>
	    <artifactId>crawljax-core</artifactId>
	    <version>{latest version from maven central}</version>
	</dependency>

Documentation
-------------

You can find more technical documentation in our [project wiki](https://github.com/crawljax/crawljax/wiki/). 


Community
---------

Keep track of development and community news.

* Follow [@crawljax](https://twitter.com/crawljax) on Twitter.
* Need a new feature or bug report? [Use the issue tracker](https://github.com/crawljax/crawljax/issues).


Changelog
---------

Detailed change history is available in our [changelog](https://github.com/crawljax/crawljax/blob/master/CHANGELOG.md).


Testing on your machine
-----------------------

You can run the tests using maven with the command

	mvn clean package
	
If you want to run all tests, including slower browser tests, run 

	mvn clean test -P integrationtests

The default driver used for testing uses Chrome so make sure you have Chrome installed on your machine). You can also specify which browser you'd like to use with the `test.browser` variable. For example, if you want to test with Firefox, use:

	mvn clean test -P integrationtests -Dtest.browser=FIREFOX

Again, make sure a recent version of Firefox is installed on your machine before running the tests.	

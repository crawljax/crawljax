#Crawljax web interface

Based on the common java web application stack. [See tutorial here](http://blog.palominolabs.com/2011/08/15/a-simple-java-web-stack-with-guice-jetty-jersey-and-jackson/)

## Running
Run the web application using the main method in `CrawljaxServer`. This will start the webapplication at [http://localhost:8080]();

### Development notes
* The project is based on Guice, which isn't implemented yet in core. It will be soon so we can already start using it from web.
* The project requires Java 7 because of Jetty 9. However, this leads to a dependency conflict with Jetty 8 that is shipped with HTMLUnit in core. We might have to switch back to Jetty 8 if this leads to any problems.
* We use JAXRS for all ajax calls. JAXRS can easily produce JSON, XML etc. For producing HTML we could use velocity or 
* To test the webappliction, refer to [this blogpost](http://alex.nederlof.com/blog/2012/11/21/integration-testing-with-jetty/)
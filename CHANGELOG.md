## [Version 2.2](https://github.com/crawljax/crawljax/issues?milestone=2&state=closed)

* [View closed issues](https://github.com/crawljax/crawljax/issues?milestone=2&page=1&state=closed)
* [View the full diff](https://github.com/crawljax/crawljax/compare/crawljax-2.1...crawljax-2.2)


## [Version 2.1](https://github.com/crawljax/crawljax/issues?milestone=1&state=closed)

### Changes and Bug fixes: 
* [View closed issues](https://github.com/crawljax/crawljax/issues?milestone=1&page=1&state=closed)
* [View the full diff](https://github.com/crawljax/crawljax/compare/crawljax-2.0...crawljax-2.1)


## Version 2.0
### New features:
* Support for concurrent multi-browser crawling. The number of browsers can be configured using the ThreadConfiguration API.
* Support added for the HTMLUnit driver. Note: this driver is very fast, but it is not a real browser.
* Added CrawlPathToException class to wrap a crawl path into a stack trace like exception giving developers / testers better insight to on which page an invariant or plugin reported an error.
* Added a Filter option to Helper.getDifferences, so a list of differences can be generated without being bothered with the changes in style attributes for example.

### Changes and Bug fixes:
* Upgraded the dependencies on Selenium (version 2.0a7).  Issue: 9.
* The initial browser was null, this was caused by the fact that the currentBrowser was not correctly set. The browser pool did not register the currentBrowser for a given thread as the browser got requested directly, as what happened with the initial browser.  Issue: 26.
* When running a (large) CrawlSpec and set a MaxRuntime constraint for it, the Crawler is not terminated directly when the MaxRuntime is reached. Basically what happened was that the current Crawler was terminated.  Afterwards all waiting Crawlers got executed and start back-tracking and when in the previous state the check  constraints terminates the Crawler. It was changed to, when the MaxRuntime is reached the current Crawler was terminated and that Crawler made a call the all other Crawlers running to shutdown and it tells the queue to empty and shutdown.  Issue: 27.
* Ignore iFrames specified by full identifier or using wild-cards.  Issue: 29.
* When using one wait condition during crawling and the first waitcondition takes a long time (> timeout) but in the end it is successful a IndexOutOfBounceException is thrown. The index is increased after an successful execution of a waitcondition and later the log event uses the increased index number to retrieve the WaitCondition that took to long to get successful.  Issue: 30.
* When supplying wrong or malformed urllocator causes an exception. Changed the behaviour not to suspect an urllocator starts with 7 characters.  Issue: 31.
* The exactEventPath got updated when OnNewState plugins where finished executing. This resulted in the wrong data being exposed to the CrawlSession. The initial fix was made to prevent this behaviour, late the CrawlPath and the ExactEventPath where merged because both lists are doing exactly the same.  Issue: 32.
* Refactored most of the Exception handling within Crawljax, when a browser crashed (for whatever reason) the Exception thrown by WebDriver was catched and ignored. Resulting in all calls to WebDriver to fail and by that flushing all the work from the queue by Crawlers using that Browser. The new implementation handles the exception at the right location and makes sure the crashed browser will be removed from the pool and won’t be used any further. The crawler where the browser crashed will be removed from the queue, limiting the number of missed states by the number that would have been found by that crawler.  Issue: 33/34.
* Fixed a lot of the FindBugs warnings, remove some legacy code (clone() support)
* Updated the WebDriver version, this was needed to support screenshots for RemoteWebDrivers. It also enables screenshotting of WebDriver instances wrapping an other WebDriver by implementing the WrapsDriver class. This enabled screenshotting in EventFiringWebDriver classes.
* Changed Crawljax Core so a CrossBrowser tool could be developed; therefore added an call to block the CrawljaxController (waitForTermination).
* When having allot elements to examine prevent getXPathExpression get called over and over again. This was solved by caching the build of the XPathâs in the current DOM. Every node in the DOM contains the full xPath leading to it self, so preventing to calculate for example the /HMTL/BODY part over and over again. Only calculate the part which has not yet been calculated.
* Rename BrowserFactory to BrowserPool which is a more correct name.

## Version 1.9
### New features:
* Support for iframe crawling. Works great in Firefox if iframes have unique IDs or NAME attributes.
* Added support for Google Chrome 4.0. Works with versions lower than 5 currently (due to WebDriver issues).
* OnFireEventFailed Plugins; enable an extension point when a fireEvent failed.

### Fixed Bugs:
* Fix a bug where underXPath would only work for XPaths that result in one element.  Issue: 16.
* Fix a bug where hashes, dots and spaces were not allowed in attribute values of clickables.  Issue: 7.
* Fix bug where wrong currentState was stored in in CrawlSession when OnInvariantViolation was called.
* Make Crawljax work with Firefox 2.0. You also need a patched webdriver. (http://code.google.com/p/selenium/issues/detail?id=387)
* Handle popups such as alert by always “clicking” OK. This is a workaround and doesn’t always work.  Issue: 2.
* Fix bug where Crawljax would write to / if the output directory was not set.
* Changed CrawljaxConfiguration to CrawljaxConfigurationReader in CrawlSession  Issue: 5.
* runOnFireEventFailedPlugins failed in some cases, prevented from happening  Issue: 8.

### Changes:
* Various improvements and polishments.
* Extended Crawljax Exceptions with more information about the system used. This is helpful to debug problems.  Issue: 9.
* Upgrade WebDriver dependency to Selenium 2.0a2.
*  Rename oracle comparators. The names are more logical now.

### Removed:
* Hibernate (database) has been removed.
* Properties file is no longer supported.  Issue: 5.

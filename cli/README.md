# Crawljax Command-line
This is the Command-line distribution of Crawljax. The project is assembled in a ZIP file containing the jar that you can run to execute the crawler.

	
Unzip the zip and in the resulting folder you can run Crawljax as follows:

```
usage: java -jar crawljax-cli-version.jar theUrl theOutputDir [OPTIONS]
   -a,--crawlHiddenAnchors     Crawl anchors even if they are not visible in the
                               browser.
   -b,--browser <arg>          browser type: chrome (default), chrome_headless, firefox, firefox_headless,
                               phantomjs
   -click <arg>                a comma separated list of HTML tags that should
                               be clicked. Default is A and BUTTON
   -d,--depth <arg>            crawl depth level. Default is 2
   -h,--help                   print this message
   -log <arg>                  Log to this file instead of the console
   -o,--override               Override the output directory if non-empty
   -p,--parallel <arg>         Number of browsers to use for crawling. Default
                               is 1
   -s,--maxstates <arg>        max number of states to crawl. Default is 0
                               (unlimited)
   -t,--timeout <arg>          Specify the maximum crawl time in minutes
   -v,--verbose                Be extra verbose
   -version                    print the version information and exit
   -waitAfterEvent <arg>       the time to wait after an event has been fired in
                               milliseconds. Default is 500
   -waitAfterReload <arg>      the time to wait after an URL has been loaded in
                               milliseconds. Default is 500
```

The output folder will contain the output of the Crawl overview plugin.
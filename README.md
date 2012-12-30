# Crawljax plugin test utils [![Build Status](https://travis-ci.org/crawljax/crawljax-test-utils.png)](https://travis-ci.org/crawljax/crawljax-test-utils)

This project offers Crawljax plugin developers a convenient way to test their plugins by offering a crawl of several default sites with known/expected output.

The jar contains three sites to test you plugin against.

1. The simple site. This site has plain \<a> links.
2. The simple JavaScript site. This site uses JavaScript to switch between states.
3. The simple Input site. This site requires the crawler to enter the right value in an input box before it changes state.

This project is open for extention to create other reusable Crawljax tests.

## Matchers
The project also contains some [matchers](https://github.com/crawljax/crawljax-test-utils/tree/master/src/main/java/com/crawljax/matchers) you can use in your own tests. Add matchers to this project if you think they are reusable.

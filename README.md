# Crawljax plugin test utils

This project offers Crawljax plugins an convenient way to test their plugins by offering a crawl of several default sites with known output.

The jar contains three sites to test you plugin against.

1. The simple site. This site has plain \<a> links.
2. The simple Javascript site. This site uses JavaScript to switch between states.
3. The simple Input site. This site requires the crawler to enter the right value in an input box before it changes state.

This project is open for extention to create other reusable Crawljax tests.

## Matchers
The proeject also contains some [matchers](https://github.com/crawljax/crawljax-plugin-test-utils/tree/master/src/main/java/com/crawljax/matchers) you can use in your own tests. Add matchers to this project if you think they are reusable.
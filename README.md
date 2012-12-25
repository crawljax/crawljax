Crawljax Plugins Parent POM
===========================

The parent Maven POM for Crawljax plugins.

Start out by adding the parent configuration to your pom:

    <parent>
      <groupId>com.crawljax.plugins</groupId>
      <artifactId>plugin</artifactId>
      <version>2.0</version>
    </parent>

The pom includes properties that allow various build configurations to be customized. 
For example, to override the default version of crawljax, just set a property:

    <properties>
      <crawljax.version>VERSION</crawljax.version>
    </properties>

For more information on how to write a Crawljax plugin see [this page](https://github.com/crawljax/crawljax/wiki/Writing-a-plugin).

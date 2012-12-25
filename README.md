Crawljax Plugins Parent POM
===========================

The parent Maven POM for Crawljax plugins.

Start out by adding the parent configuration to your pom:

    <parent>
      <groupId>com.crawljax.plugins</groupId>
      <artifactId>plugin</artifactId>
      <version>2.1</version>
    </parent>

The pom includes properties that allow various build configurations to be customized. 
For example, to override the default version of crawljax, just set a property:

    <properties>
      <crawljax.version>VERSION</crawljax.version>
    </properties>

For more information on how to write a Crawljax plugin see [this page](https://github.com/crawljax/crawljax/wiki/Writing-a-plugin).

The parent pom unfortunately isn't available in a public repository yet. Until it is, you also need to declare the repository in your pom.

    <repositories>
        <repository>
            <id>crawljax.mvn.repo</id>
            <url>https://github.com/crawljax/crawljax-mvn-repo/raw/master</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>

Crawljax Maven Archtypes
========================

Crawljax Plugin
---------------

To generate a plugin project template using Maven:

     mvn archetype:generate \
       -DgroupId=sample \
       -DartifactId=sample-plugin \
       -DarchetypeGroupId=com.crawljax.plugins.archetypes \
       -DarchetypeArtifactId=crawljax-plugins-archetype \
       -DarchetypeVersion=1.1
       
You can change the values of ``-DgroupId`` and ``-DartifactId`` to reflect your project's package and name. 

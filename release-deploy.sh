#!/bin/bash
echo Releasing...
TEMP_DIR=deployment
rm -rf $TEMP_DIR
mkdir $TEMP_DIR
cd $TEMP_DIR &> /dev/null
echo $(pwd)
svn co https://svn.st.ewi.tudelft.nl/svn/spci/trunk/code/crawljax
cd /crawljax
echo $(pwd)
mvn release:prepare -Ddryrun=true
mvn release:perform -Ddrytun=true
cd /target/checkout
mvn deploy
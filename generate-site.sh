#!/bin/bash
set -e
mvn site
cd ../davidmoten.github.io
git pull
mkdir rxjava-slf4j
cp -r ../rxjava-slf4j/target/site/* rxjava-slf4j/
git add .
git commit -am "update site reports"
git push

#!/bin/bash
top=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd ${top}

for x in $(find . -maxdepth 2 -name pom.xml)
do
  echo building $x
  cd $(dirname $x)
  ../mvnw package
  cd $top
done

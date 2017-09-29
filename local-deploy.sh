#!/bin/bash

# Extract all the variables that we need.
pluginV=$(grep "pluginVersion =" build.sbt | sed 's/.*"\(.*\)"/\1/')
scalaV=$(grep "scalaVersion :=" build.sbt | sed 's/.*"\(.*\..*\)\..*",/\1/')
sbtV=$(grep "sbt.version" project/build.properties | sed 's/.*=\(.*\..*\)\..*/\1/')
flywaydbPath="../flywaydb.org/repo/org/flywaydb"
root=${flywaydbPath}/flyway-sbt_${scalaV}_${sbtV}/${pluginV}
ivyRoot=~/.ivy2/local/org.flywaydb/flyway-sbt/scala_${scalaV}/sbt_${sbtV}/${pluginV}

mkdir -p ${root}
cp ${ivyRoot}/jars/flyway-sbt.jar ${root}/flyway-sbt-${pluginV}.jar
cp ${ivyRoot}/srcs/flyway-sbt-sources.jar ${root}/flyway-sbt-${pluginV}-sources.jar
cp ${ivyRoot}/poms/flyway-sbt.pom ${root}/flyway-sbt-${pluginV}.pom

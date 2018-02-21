
[![Travis](https://img.shields.io/travis/flyway/flyway-sbt.svg)](https://travis-ci.org/flyway/flyway-sbt)

### Sbt 1.x plugin for [Flyway](https://flywaydb.org)

Welcome to the home for the `sbt` v1.x plugin for flyway. The [user manual](https://davidmweber.github.io/flyway-sbt-docs/)
will tell you how to get started. This project is based on the original 
[flyway-sbt](https://github.com/flyway/flyway/tree/master/flyway-sbt) that was in the flyway repository through 
version 4.2.1.

Build and testing uses `sbt` and it's plugin [testing framework](http://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html). 
The test cases are pretty basic (hint: we need more of those). There is no support for `sbt` prior to 1.0. Use the 
[legacy plugin](https://github.com/flyway/flyway/tree/master/flyway-sbt) instead.

Note that from v5.0.0 onwards, the plugin has to be explicitly enabled using `enablePlugins(FlywayPlugin)`. This prevents
Flyway actions triggering unrelated build activity and addresses [this issue](https://github.com/flyway/flyway/issues/1329).

Build and test the plugin using

```bash
sbt scripted
```

Early adopters should just publish a clone or fork of this repository locally:
```bash
git clone https://github.com/flyway/flyway-sbt.git
cd flyway-sbt
sbt publishLocal
```

Deployment is via Flyway's website (https://github.com/flyway/flywaydb.org). Build as above for early adopters and then:

```bash
./local-deploy.sh
```

This will copy the artefacts to the right place in the flywaydb.org repo. Commit flywaydb.org and make a pull request.

The plugin can also be obtained by adding the following to your `project/plugin.sbt` file:

```scala
addSbtPlugin("org.flywaydb" % "flyway-sbt" % "5.0.0-RC1")
```


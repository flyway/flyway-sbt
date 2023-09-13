organization := "org.flywaydb"
enablePlugins(FlywayPlugin)
name := "flyway-sbt-test3"

libraryDependencies ++= Seq(
  "org.hsqldb" % "hsqldb" % "2.5.0"
)

flywayUrl := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true"
flywayUser := "SA"
flywayLocations := Seq("filesystem:src/main/resources/db/migration")
flywayUrl in Test := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true"
flywayUser in Test := "SA"
flywayLocations in Test := Seq("filesystem:src/main/resources/db/migration")
flywayCleanDisabled := false
Test / flywayCleanDisabled := false

val flywayVersion = "4.2.0"
val pluginVersion = "4.2.0"

lazy val root = (project in file ("."))
    .settings(
      sbtPlugin := true,
      name := "flyway-sbt",
      scalaVersion := "2.12.3",
      organization := "org.flywaydb",
      version := pluginVersion,
      libraryDependencies ++= Seq(
        "org.flywaydb" % "flyway-core" % flywayVersion
      ),
      scriptedLaunchOpts := { scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false
  )


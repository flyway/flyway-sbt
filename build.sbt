val flywayVersion = "4.2.0"
val pluginVersion = "4.2.0"

lazy val root = (project in file ("."))
    .settings(
      sbtPlugin := true,
      name := "flyway-sbt",
      organization := "org.flywaydb",
      version := pluginVersion,
      libraryDependencies ++= Seq(
        "org.flywaydb" % "flyway-core" % flywayVersion
      ),
      scalacOptions ++= Seq(
        "-deprecation",
        "-unchecked",
        "-Xfuture"
      ),
      scriptedLaunchOpts := { scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false
  )


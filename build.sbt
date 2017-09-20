val flywayVersion = "4.2.0"
val pluginVersion = "0.0.0-SNAPSHOT"

lazy val root = (project in file ("."))
    .settings(
      sbtPlugin := true,
      name := "flyway-sbt",
      organization := "org.flywaydb",
      version := pluginVersion,
      resolvers += (
        "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
      ),
      libraryDependencies ++= Seq(
        "org.flywaydb" % "flyway-core" % flywayVersion
      ),
      scriptedLaunchOpts := { scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false
  )


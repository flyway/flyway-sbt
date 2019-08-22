val flywayVersion = "5.2.4"
val pluginVersion = "5.2.4"

lazy val root = (project in file ("."))
    .enablePlugins(SbtPlugin)
    .settings(
      name := "flyway-sbt",
      organization := "io.github.davidmweber",
      version := pluginVersion,
      libraryDependencies ++= Seq(
        "org.flywaydb" % "flyway-core" % flywayVersion
      ),
      scalacOptions ++= Seq(
        "-deprecation",
        "-unchecked",
        "-Xfuture"
      ),
      scalacOptions in (Compile, doc) ++= {
        Seq(
          "-sourcepath",
          (baseDirectory in LocalRootProject).value.getAbsolutePath,
          "-doc-source-url",
          s"""https://github.com/flyway/flyway-sbt/tree/${sys.process.Process("git rev-parse HEAD").lineStream_!.head}€{FILE_PATH}.scala"""
        )
      },
      scriptedLaunchOpts := { scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false
  )


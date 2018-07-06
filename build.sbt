val flywayVersion = "5.1.1" // Remember to update this in docs/_config.yml and sbt-test/flyway-sbt/test{1|2}/build.sbt
val pluginVersion = "5.0.0"

lazy val root = (project in file ("."))
    .settings(
      sbtPlugin := true,
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


import xerial.sbt.Sonatype.GithubHosting

sonatypeProfileName := "io.github.davidmweber"
publishMavenStyle := true
sonatypeProjectHosting := Some(GithubHosting(user="flyway", repository="flyway-sbt", email="dave@veryflatcat.com"))
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
publishTo := sonatypePublishTo.value


// sbt publishSigned
// sbt sonatypeRelease

pomExtra :=
    <developers>
      <developer>
        <id>davidmweber</id>
        <name>David Weber</name>
        <url>https://github.com/flyway/flyway-sbt</url>
      </developer>
    </developers>

import xerial.sbt.Sonatype._

sonatypeProfileName := "io.github.davidmweber"
publishMavenStyle := true
sonatypeProjectHosting := Some(GitHubHosting(user="flyway", repository="flyway-sbt", email="dave@veryflatcat.com"))
//developers := List(
//  Developer(id="davidmweber", name="David Weber", email="dave@veryflatcat.com", url=url("https://davidmweber.github.io/flyway-sbt-docs/"))
//)
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
publishTo := sonatypePublishTo.value

// sbt publishSigned
// sbt sonatypeRelease

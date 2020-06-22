import Dependencies._

name := "circe-zstream"

ThisBuild / publishTo := sonatypePublishToBundle.value

inThisBuild(
  List(
    organization := "io.github.anakos",
    scalaVersion := "2.13.2",
    homepage := Some(url("https://github.com/anakos/circe-zstream")),
    scmInfo := Some(ScmInfo(url("https://github.com/anakos/circe-zstream"), "git@github.com:anakos/circe-zstream.git")),
    developers := List(Developer("anakos", "Alexander Nakos", "anakos@gmail.com", url("https://github.com/anakos"))),
    licenses += ("MIT", url("https://github.com/anakos/circe-zstream/blob/master/LICENSE")),
    publishMavenStyle := true,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
)

lazy val root = (project in file("."))
  .settings(
    publish         := {},
    publishLocal    := {},
    publishArtifact := false,
  )
  .aggregate(core, benchmark)


lazy val core =
  Project("circe-zstream", file("lib"))
    .settings(
      libraryDependencies ++= List(
        circe.core,
        circe.generic,
        circe.jawn,
        zio.core,
        zio.streams,
        zio.test      % Test,
        zio.test_sbt  % Test,
      )
    )

lazy val benchmark =
  Project("circe-zstream-benchmark", file(s"benchmark"))
    .settings(
      publish         := {},
      publishLocal    := {},
      publishArtifact := false,
      libraryDependencies ++= List(
        "co.fs2"   %% "fs2-core"  % "2.4.1",
        "co.fs2"   %% "fs2-io"    % "2.4.1",
        "io.circe" %% "circe-fs2" % "0.13.0",
        circe.core,
        circe.jawn,
        zio.core,
        zio.streams,
        zio.test     % Test,
        zio.test_sbt % Test,
      )
    )
    .enablePlugins(JmhPlugin)
    .dependsOn(core)

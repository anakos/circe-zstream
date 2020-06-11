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
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
)

libraryDependencies ++= List(
  cats.core,
  circe.core,
  circe.generic,
  circe.jawn,
  zio.core,
  zio.streams,
  zio.test          % Test,
  zio.test_sbt      % Test,
)

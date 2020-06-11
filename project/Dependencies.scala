import sbt._

object Dependencies {
  object cats {
    def mkModule(name: String) =
      "org.typelevel" %% s"cats-${name}" % "2.2.0-M2"

    val core    = mkModule("core")
  }  

  object circe {
    def mkModule(name: String) =
      "io.circe" %% s"circe-${name}" % "0.13.0"

    val core     = mkModule("core")
    val generic  = mkModule("generic")
    val jawn     = mkModule("jawn")
  }

  object zio {
    def mkModule(name: String) =
      "dev.zio" %% name % "1.0.0-RC20+51-197673dc-SNAPSHOT"

    val core     = mkModule("zio")
    val streams  = mkModule("zio-streams")
    val test     = mkModule("zio-test")
    val test_sbt = mkModule("zio-test-sbt")
  }
}

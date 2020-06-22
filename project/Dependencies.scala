import sbt._

object Dependencies {
  object circe {
    def mkModule(name: String) =
      "io.circe" %% s"circe-${name}" % "0.14.0-M1"

    val core     = mkModule("core")
    val generic  = mkModule("generic")
    val jawn     = mkModule("jawn")
  }

  object zio {
    def mkModule(name: String) =
      "dev.zio" %% name % "1.0.0-RC21"

    val core     = mkModule("zio")
    val streams  = mkModule("zio-streams")
    val test     = mkModule("zio-test")
    val test_sbt = mkModule("zio-test-sbt")
  }
}

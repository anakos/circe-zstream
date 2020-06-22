package benchmarks

import io.circe.syntax._
import zio.test._
import zio.test.Assertion._

object BenchmarkSpec extends DefaultRunnableSpec {
  override def spec =
    suite("Benchmark code correctness")(
      suite("zstream benchmarks")(
        test("parseFoos") {
          val bm = new ZStreamBenchmark
          assert(bm.parseFoos)(
            equalTo(
              Right(zio.Chunk.single(bm.foosC))
            )
          )
        },
        test("parseInts") {
          val bm = new ZStreamBenchmark
          assert(bm.parseInts)(
            equalTo(
              Right(
                zio.Chunk.fromIterable(
                  bm.ints.map(_.asJson)
                )
              )
            )
          )
        },
        test("parseInts2") {
          val bm = new ZStreamBenchmark
          assert(bm.parseInts2)(
            equalTo(
              Right(
                zio.Chunk.single(bm.intsC)
              )
            )
          )
        },
        test("decodeFoos") {
          val bm = new ZStreamBenchmark
          assert(bm.decodeFoos)(
            equalTo(
              Right(
                zio.Chunk.single(bm.foos)
              )
            )
          )
        },        
        test("decodeInts") {
          val bm = new ZStreamBenchmark
          assert(bm.decodeInts)(
            equalTo(
              Right(
                zio.Chunk.fromIterable(bm.ints)
              )
            )
          )
        },
        test("decodeInts2") {
          val bm = new ZStreamBenchmark
          assert(bm.decodeInts2)(
            equalTo(
              Right(
                zio.Chunk.single(bm.ints.toVector)
              )
            )
          )
        },
      ),

      suite("fs2 benchmarks")(
        test("parseFoos") {
          val bm = new FS2Benchmark
          assert(bm.parseFoos)(
            equalTo(
              Right(Vector(bm.foosC))
            )
          )
        },
        test("parseInts") {
          val bm = new FS2Benchmark
          assert(bm.parseInts)(
            equalTo(
              Right(
                bm.ints.map(_.asJson).toVector
              )
            )
          )
        },
        test("parseInts2") {
          val bm = new FS2Benchmark
          assert(bm.parseInts2)(
            equalTo(
              Right(Vector(bm.intsC))
            )
          )
        },        
        test("decodeFoos") {
          val bm = new FS2Benchmark
          assert(bm.decodeFoos)(
            equalTo(
              Right(Vector(bm.foos))
            )
          )
        },
        test("decodeInts") {
          val bm = new FS2Benchmark
          assert(bm.decodeInts)(
            equalTo(
              Right(
                bm.ints.toVector
              )
            )
          )
        },
        test("decodeInts2") {
          val bm = new FS2Benchmark
          assert(bm.decodeInts2)(
            equalTo(
              Right(
                Vector(bm.ints.toVector)
              )
            )
          )
        },        
    )
  )
}

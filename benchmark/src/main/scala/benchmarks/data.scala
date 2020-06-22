package benchmarks

import io.circe._
import org.openjdk.jmh.annotations._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import org.typelevel.jawn.AsyncParser
import zio.{circe => Zirce}
import java.util.concurrent.ExecutorService

class ExampleData {
  val ints: List[Int] = (0 to 1000).toList

  val foos: Map[String, Foo] = List
    .tabulate(1000) { i =>
      ("b" * i) -> Foo("a" * i, (i + 2.0) / (i + 1.0), i, i * 1000L, (0 to i).map(_ % 2 == 0).toList)
    }
    .toMap

  val intsC: Json = Encoder[List[Int]].apply(ints)
  val intsJson: String           = intsC.noSpaces
  val intsJsonBytes: Array[Byte] = intsJson.getBytes(Zirce.text.utf8Charset)

  val foosC: Json = Encoder[Map[String, Foo]].apply(foos)
  val foosJson: String           = foosC.noSpaces
  val foosJsonBytes: Array[Byte] = foosJson.getBytes(Zirce.text.utf8Charset)
}

final case class Foo(s: String, d: Double, i: Int, l: Long, bs: List[Boolean])
object Foo {
  implicit val encoder: Encoder[Foo] =
    io.circe.generic.semiauto.deriveEncoder
  implicit val decoder: Decoder[Foo] =
    io.circe.generic.semiauto.deriveDecoder  
}

trait ZStreamParsingBenchmarks { self: ExampleData =>
  import zio._
  import zio.blocking.Blocking
  import zio.stream._

  lazy val foosStream =
    zioByteStream(foosJsonBytes, DefaultChunkSize)
  lazy val intsStream =
    zioByteStream(intsJsonBytes, DefaultChunkSize)

  @Benchmark
  def parseFoos: Either[Throwable, Chunk[Json]] =
    run(
      (foosStream >>> Zirce.byteParser(AsyncParser.SingleValue))
        .runCollect
        .either
    )

  @Benchmark
  def parseInts: Either[Throwable, Chunk[Json]] =
    run(
      (intsStream >>> Zirce.byteArrayParser)
        .runCollect
        .either
    )  

  @Benchmark
  def parseInts2: Either[Throwable, Chunk[Json]] =
    run(
      (intsStream >>> Zirce.byteParser(AsyncParser.SingleValue))
        .runCollect
        .either
    ) 

  @Benchmark
  def decodeFoos: Either[Throwable, Chunk[Map[String, Foo]]] =
    run(
      (foosStream >>> Zirce.byteParser(AsyncParser.SingleValue) >>> Zirce.decoder[Map[String, Foo]])
        .runCollect
        .either
    )

  @Benchmark
  def decodeInts: Either[Throwable, Chunk[Int]] =
    run(
      (intsStream >>> Zirce.byteArrayParser >>> Zirce.decoder[Int])
        .runCollect
        .either
    )    

  @Benchmark
  def decodeInts2: Either[Throwable, Chunk[Vector[Int]]] =
    run(
      (intsStream >>> Zirce.byteParser(AsyncParser.SingleValue) >>> Zirce.decoder[Vector[Int]])
        .runCollect
        .either
    ) 

  def run[A](input: URIO[Blocking, Either[Throwable, A]]): Either[Throwable, A] =
    Runtime.default.unsafeRun(input)

  def zioByteStream(bytes: Array[Byte], chunkSize: Int): ZStream[Blocking, Throwable, Byte] =
    ZStream.fromInputStream(
      new java.io.ByteArrayInputStream(bytes),
      chunkSize
    )  
}

trait FS2ParsingBenchmarks { self: ExampleData =>
  import cats.effect._
  import _root_.fs2._
  import _root_.io.circe.fs2._

  lazy val foosStream =
    toByteStream(foosJsonBytes, DefaultChunkSize)
  lazy val intsStream =
    toByteStream(intsJsonBytes, DefaultChunkSize)  

  @Benchmark
  def parseFoos: Either[Throwable, Vector[Json]] =
    foosStream.through(byteParser(AsyncParser.SingleValue))
      .compile
      .toVector
      .attempt
      .unsafeRunSync()

  @Benchmark
  def parseInts: Either[Throwable, Vector[Json]] =
    intsStream.through(byteArrayParser)
      .compile
      .toVector
      .attempt
      .unsafeRunSync()  

  @Benchmark
  def parseInts2: Either[Throwable, Vector[Json]] =
    intsStream
      .through(byteParser(AsyncParser.SingleValue))
      .compile
      .toVector
      .attempt
      .unsafeRunSync()  

  @Benchmark
  def decodeFoos: Either[Throwable, Vector[Map[String, Foo]]] =
    foosStream.through(byteParser(AsyncParser.SingleValue))
      .through(decoder[IO, Map[String, Foo]])
      .compile
      .toVector
      .attempt
      .unsafeRunSync()  

  @Benchmark
  def decodeInts: Either[Throwable, Vector[Int]] =
    intsStream.through(byteArrayParser)
      .through(decoder[IO, Int])
      .compile
      .toVector
      .attempt
      .unsafeRunSync()  

  @Benchmark
  def decodeInts2: Either[Throwable, Vector[Vector[Int]]] =
    intsStream.through(byteParser(AsyncParser.SingleValue))
      .through(decoder[IO, Vector[Int]])
      .compile
      .toVector
      .attempt
      .unsafeRunSync()  

  def toByteStream(bytes: Array[Byte], chunkSize: Int): Stream[IO, Byte] = 
    withBlockingEC { blockingEC =>
      implicit val cs = IO.contextShift(ExecutionContext.global)
      io.readInputStream[IO](
        cats.effect.IO.delay { new java.io.ByteArrayInputStream(bytes) },
        chunkSize,
        cats.effect.Blocker.liftExecutionContext(
          scala.concurrent.ExecutionContext.fromExecutor(blockingEC)
        )
      )
    }

  def withBlockingEC[C](f: ExecutorService => Stream[IO, C]): Stream[IO, C] =
    Stream.bracket(IO { Executors.newSingleThreadExecutor() })(blockingEC => IO.delay(blockingEC.shutdown()))
      .flatMap(f)
}
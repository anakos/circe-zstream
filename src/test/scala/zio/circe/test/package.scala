package zio
package circe

import cats.syntax.either._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.typelevel.jawn.AsyncParser
import zio.stream._
import zio.test.{checkAllM,Gen, assertM  /*, TestResult*/ }
import zio.test.Assertion._
import zio.circe.examples.Foo

package object test {
  // also string stream
  def fooStream[A](fooStdStream: Vector[A], fooVector: Vector[A]) =
    Stream.fromIterable(fooStdStream) ++ Stream.fromIterable(fooVector)

  def serializeFoos[A : Encoder](parsingMode: AsyncParser.Mode, foos: Stream[Nothing, A]) =
    parsingMode match {
      case AsyncParser.ValueStream | AsyncParser.SingleValue =>
        foos.map(_.asJson.spaces2).intersperse("\n")
      case AsyncParser.UnwrapArray =>
        foos.map(_.asJson.spaces2)
          .intersperse("[", ",", "]")
    }

  def testParser(mode: AsyncParser.Mode, through: Transducer[Throwable, String, Json]) =
    checkAllM(vector(Foo.gen), vector(Foo.gen)) {
      (fooStdStream: Vector[Foo], fooVector: Vector[Foo]) =>
        val stream = serializeFoos(mode, fooStream(fooStdStream, fooVector))
        val foos   = (fooStdStream ++ fooVector).map(_.asJson)

        verifyStreamElements(stream >>> through, foos.toList)
    }
  
  def testParsingFailure(through: Transducer[Throwable, String, Json]) =
    checkAllM(vector(Gen.anyString), vector(Gen.anyString)) { (stringStdStream, stringVector) =>
      assertM(
        (Stream("}") ++ fooStream(stringStdStream, stringVector) >>> through)
          .runCollect
          .run
      )(fails(isSubtype[ParsingFailure](anything)))
    }
  
  def vector[R, A](gen: Gen[R, A]) =
    Gen.listOfBounded(0,1000)(gen)
      .map { _.toVector }

  def verifyStreamElements[E, A](stream: Stream[Throwable,A], expectedElements: List[A]) =
    assertM(stream.runCollect.either)(equalTo(Chunk.fromIterable(expectedElements).asRight))

  // TODO: submit a PR for this?
  /** Encodes a stream of `String` in to a stream of bytes using the given charset. */
  def encode(charset: java.nio.charset.Charset): Transducer[Throwable, String, Byte] =
    ZTransducer.fromPush {
      case None      =>
        ZIO.succeed(Chunk.empty)
      case Some(str) =>
        ZIO { str.flatMap(in => Chunk.fromArray(in.getBytes(charset))) }
    }  

  /** Encodes a stream of `String` in to a stream of `Chunk[Byte]` using the given charset. */
  def encodeC(charset: java.nio.charset.Charset) =
    ZTransducer
      .fromFunctionM { in: String => ZIO { Chunk.fromArray(in.getBytes(charset)) } }

  private val utf8Charset = java.nio.charset.Charset.forName("UTF-8")
  /** Encodes a stream of `String` in to a stream of bytes using the UTF-8 charset. */
  def utf8Encode = encode(utf8Charset)

  /** Encodes a stream of `String` in to a stream of `Chunk[Byte]` using the UTF-8 charset. */
  def utf8EncodeC = encodeC(utf8Charset)
}
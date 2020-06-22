package zio
package circe

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
    assertM(stream.runCollect.either)(equalTo(Right(Chunk.fromIterable(expectedElements))))
}
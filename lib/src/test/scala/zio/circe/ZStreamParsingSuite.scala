package zio
package circe

import io.circe.syntax._
import io.circe.generic.auto._
import org.typelevel.jawn.AsyncParser
import zio.circe.examples._
import zio.circe.test._
import zio.circe.text._
import zio.stream._
import zio.test._
import zio.test.Assertion

object ZStreamParsingSuite extends zio.test.DefaultRunnableSpec {
  override def spec =
    suite("Streaming Circe Parser")(
      suite("strings")(
        suite("stringArrayParser")(
          testM("parses values wrapped in array")(
            testParser(AsyncParser.UnwrapArray, stringArrayParser)
          ),
          testM("fails with ParsingFailure when supplied invalid JSON")(
            testParsingFailure(stringArrayParser)
          ),
        ),
        suite("stringStreamParser")(
          testM("parses values delimited by new lines")(
            testParser(AsyncParser.ValueStream, stringStreamParser)
          ),
          testM("fails with ParsingFailure when supplied invalid JSON")(
            testParsingFailure(stringStreamParser)
          ),
        ),
        suite("stringParser")(
          testM("parses single value")(
            checkAllM(Foo.gen) { foo =>
              verifyStreamElements(
                serializeFoos(AsyncParser.SingleValue, ZStream(foo)) >>> stringParser(AsyncParser.SingleValue),
                List(foo.asJson)
              )
            }
          ),
        )
      ),

      suite("bytes")(
        suite("byteArrayParser")(
          testM("parses bytes wrapped in array")(
            testParser(AsyncParser.UnwrapArray, utf8Encode >>> byteArrayParser)
          ),
          testM("fails with ParsingFailure when supplied invalid JSON")(
            testParsingFailure(utf8Encode >>> byteArrayParser)
          ),
        ),
        suite("byteStreamParser")(
          testM("parses bytes delimited by new lines")(
            testParser(AsyncParser.ValueStream, utf8Encode >>> byteStreamParser)
          ),
          testM("fails with ParsingFailure when supplied invalid JSON")(
            testParsingFailure(utf8Encode >>> byteStreamParser)
          ),
        ),
        suite("byteParser")(
          testM("parses single value")(
            checkAllM(Foo.gen) { foo =>
              verifyStreamElements(
                serializeFoos(AsyncParser.SingleValue, ZStream(foo)) >>> utf8Encode >>> byteParser(AsyncParser.SingleValue),
                List(foo.asJson)
              )
            }
          ),
          testM("parses single value, when run twice")(
            checkAllM(Foo.gen) { foo =>
              val parseOnce = serializeFoos(AsyncParser.SingleValue, ZStream(foo)) >>>
                  utf8Encode >>>
                  byteParser(AsyncParser.SingleValue)
 
              for {
                result1 <- assertM(parseOnce.runCollect.either)(Assertion.equalTo(Right(Chunk(foo.asJson))))
                result2 <- assertM(parseOnce.runCollect.either)(Assertion.equalTo(Right(Chunk(foo.asJson))))
              } yield result1 && result2
            }
          ),
        ),
        suite("byteArrayParserC")(
          testM("parses bytes wrapped in array")(
            testParser(AsyncParser.UnwrapArray, utf8EncodeC >>> byteArrayParserC)
          ),
          testM("fails with ParsingFailure when supplied invalid JSON")(
            testParsingFailure(utf8EncodeC >>> byteArrayParserC)
          ),          
        ),
        suite("byteStreamParserC")(
          testM("parses bytes delimited by new lines")(
            testParser(AsyncParser.ValueStream, utf8EncodeC >>> byteStreamParserC)
          ),
          testM("fails with ParsingFailure when supplied invalid JSON")(
            testParsingFailure(utf8EncodeC >>> byteStreamParserC)
          ),          
        ),
        suite("byteParserC")(
          testM("parses single value")(
            checkAllM(Foo.gen) { foo =>
              verifyStreamElements(
                serializeFoos(AsyncParser.SingleValue, ZStream(foo)) >>> utf8EncodeC >>> byteParserC(AsyncParser.SingleValue),
                List(foo.asJson)
              )
            }
          ),
        ),
      ),
      testM("decode enumerated JSON values")(
        checkAllM(vector(Foo.gen), vector(Foo.gen)) { (l, r) =>
          verifyStreamElements(
            serializeFoos(AsyncParser.UnwrapArray, fooStream(l, r)) >>>
              stringArrayParser >>>
              decoder[Foo],
            (l ++ r).toList
          )
        }
      ),

      suite("encoding")(
        testM("utf8Encode >>> utf8Decode = id")(
          checkAllM(Gen.anyString) { s =>
            val roundtrip = (Stream(s) >>> utf8Encode >>> ZTransducer.utf8Decode)
              .runCollect
              .map(_.toList) 
            assertM(roundtrip)(Assertion.equalTo(List(s)))
          }
        )
      )
    )
}
package zio

import io.circe._
import io.circe.jawn.CirceSupportParser

import org.typelevel.jawn._

import zio._
import zio.stream._

package object circe {
  final def stringArrayParser: Transducer[Throwable, String, Json] =
    stringParser(AsyncParser.UnwrapArray)

  final def stringStreamParser: Transducer[Throwable, String, Json] =
    stringParser(AsyncParser.ValueStream)

  final def stringParser(mode: AsyncParser.Mode): Transducer[Throwable, String, Json] =
    transduce(
      mode,
      parser => in => parser.absorb(in.toList.mkString)(CirceSupportParser.facade)
    )

  final def byteArrayParser: Transducer[Throwable, Byte, Json] =
    byteParser(AsyncParser.UnwrapArray)

  final def byteStreamParser: Transducer[Throwable, Byte, Json] =
    byteParser(AsyncParser.ValueStream)

  final def byteParser(mode: AsyncParser.Mode): Transducer[Throwable, Byte, Json] =
    transduce(
      mode,
      parser => in => parser.absorb(in.toArray)(CirceSupportParser.facade)
    )

  final def byteArrayParserC: Transducer[Throwable, Chunk[Byte], Json] =
    byteParserC(AsyncParser.UnwrapArray)

  final def byteStreamParserC: Transducer[Throwable, Chunk[Byte], Json] =
    byteParserC(AsyncParser.ValueStream)

  final def byteParserC(mode: AsyncParser.Mode): Transducer[Throwable, Chunk[Byte], Json] =
    transduce(
      mode,
      parser => in => parser.absorb(in.flatten.toArray)(CirceSupportParser.facade)
    )

  final def decoder[A](implicit decode: Decoder[A]): Transducer[Throwable, Json, A] =
    ZTransducer.fromFunctionM(json => ZIO.fromEither(decode(json.hcursor)))

  final def transduce[S](
      parsingMode: AsyncParser.Mode,
      parseWith: AsyncParser[Json] => Chunk[S] => Either[ParseException, collection.Seq[Json]]
  ) = {
    def newParser = CirceSupportParser.async(parsingMode)

    def parse(state: Ref[Option[AsyncParser[Json]]]): Option[Chunk[S]] => Task[Chunk[Json]] = {
      case None =>
        for {
          next <- state.modify {
            case None    => (Right(Chunk.empty), None)
            case Some(p) => (p.finish()(CirceSupportParser.facade).map(Chunk.fromIterable(_)), None)
          }
          result <- ZIO.fromEither(next)
        } yield result

      case Some(in) =>
        for {
          next <- state.modify { p =>
            val parser = p.getOrElse(newParser)
            val result = parseWith(parser)(in).left.map(ex => ParsingFailure(ex.getMessage, ex))

            (result, Some(parser))
          }
          result <- ZIO.fromEither(next)
        } yield Chunk.fromIterable(result)
    }

    ZTransducer[Any, Throwable, S, Json] {
      ZRef.makeManaged[Option[AsyncParser[Json]]](None)
        .map { parserRef => parse(parserRef) }
    }
  }  
}
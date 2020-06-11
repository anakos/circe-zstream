package zio

import cats.instances.all._
import cats.syntax.option._
import cats.syntax.either._
import cats.syntax.traverse._

import io.circe._
import io.circe.jawn.CirceSupportParser

import org.typelevel.jawn._

import zio._
import zio.stream._

package object circe {
  private[circe] def chunk[A]: Transducer[Nothing, A, Chunk[A]] =
    Transducer.fromPush {
      case None        => ZIO.succeedNow(Chunk.empty)
      case Some(chunk) => ZIO.succeedNow(Chunk(chunk))
    }  

  final def stringArrayParser: Transducer[Throwable, String, Json] =
    stringParser(AsyncParser.UnwrapArray)

  final def stringStreamParser: Transducer[Throwable, String, Json] =
    stringParser(AsyncParser.ValueStream)

  final def stringParser(mode: AsyncParser.Mode): Transducer[Throwable, String, Json] =
    transducer(mode, parser => in => parser.absorb(in)(CirceSupportParser.facade))

  final def byteArrayParser: Transducer[Throwable, Byte, Json] =
    byteParser(AsyncParser.UnwrapArray)

  final def byteStreamParser: Transducer[Throwable, Byte, Json] =
    byteParser(AsyncParser.ValueStream)

  final def byteParser(mode: AsyncParser.Mode): Transducer[Throwable, Byte, Json] =
    chunk >>> byteParserC(mode)

  final def byteArrayParserC: Transducer[Throwable, Chunk[Byte], Json] =
    byteParserC(AsyncParser.UnwrapArray)

  final def byteStreamParserC: Transducer[Throwable, Chunk[Byte], Json] =
    byteParserC(AsyncParser.ValueStream)

  final def byteParserC(mode: AsyncParser.Mode): Transducer[Throwable, Chunk[Byte], Json] =
    transducer(mode, parser => in => parser.absorb(in.toArray)(CirceSupportParser.facade))

  final def decoder[A](implicit decode: Decoder[A]): Transducer[Throwable, Json, A] =
    ZTransducer.fromFunctionM(json => ZIO.fromEither(decode(json.hcursor)))

  final def transducer[S](parsingMode: AsyncParser.Mode,
                          parseWith: AsyncParser[Json] => S => Either[ParseException, collection.Seq[Json]]) = {
    def newParser = CirceSupportParser.async(parsingMode)

    def go(state: Ref[Option[AsyncParser[Json]]]): Option[Chunk[S]] => Task[Chunk[Json]] = {
      case None =>
        for {
          next <- state.modify {
            case None    => (Chunk.empty.asRight, none)
            case Some(p) => (p.finish()(CirceSupportParser.facade).map(Chunk.fromIterable(_)), none)
          }
          result <- ZIO.fromEither(next)
        } yield result

      case Some(in) =>
        for {
          next <- state.modify { p =>
            val parser = p.getOrElse(newParser)
            val result = in.toList.traverse(parseWith(parser))
              .bimap(
                ex => ParsingFailure(ex.getMessage, ex),
                x  => Chunk.fromIterable(x.flatten)
              )

            (result, parser.some)
          }
          result <- ZIO.fromEither(next)
        } yield result
    }

    ZTransducer[Any, Throwable, S, Json] {
      ZRef.makeManaged(none[AsyncParser[Json]])
        .map { parserRef => go(parserRef) }
    }
  }  
}
package zio
package circe

import java.nio.charset.Charset
import zio.stream._

object text {
  val utf8Charset = Charset.forName("UTF-8")

  /** Encodes a stream of `String` in to a stream of bytes using the given charset. */
  def encode(charset: Charset): Transducer[Throwable, String, Byte] =
    ZTransducer.fromPush {
      case None      =>
        Transducer.Push
          .next
      case Some(str) =>
        Transducer.Push
          .emit(str.flatMap(in => Chunk.fromArray(in.getBytes(charset))))
    }  

  /** Encodes a stream of `String` in to a stream of `Chunk[Byte]` using the given charset. */
  def encodeC(charset: java.nio.charset.Charset) =
    ZTransducer
      .fromFunctionM { in: String => ZIO { Chunk.fromArray(in.getBytes(charset)) } }


  /** Encodes a stream of `String` in to a stream of bytes using the UTF-8 charset. */
  def utf8Encode = encode(utf8Charset)

  /** Encodes a stream of `String` in to a stream of `Chunk[Byte]` using the UTF-8 charset. */
  def utf8EncodeC = encodeC(utf8Charset)  
}

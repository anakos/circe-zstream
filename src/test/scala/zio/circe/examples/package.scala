package zio.circe

import cats._
import cats.instances.all._
// import io.circe._
import zio.random._
import zio.test._

package object examples {
  implicit val eqThrowable: Eq[Throwable] = Eq.fromUniversalEquals
}

package examples {
  final case class Box[A](a: A)
  object Box {
    implicit def eqBox[A: Eq]: Eq[Box[A]] = Eq.by(_.a)

    def genBox[A](genA: Gen[Random, A]): Gen[Random, Box[A]] =
      genA.map(Box(_))
  }
  
  final case class Qux[A](i: Int, a: A, j: Int)
  object Qux {
    implicit def eqQux[A: Eq]: Eq[Qux[A]] = Eq.by(q => (q.i, q.a, q.j))

    def genQux[A](genA: Gen[Random, A]): Gen[Random, Qux[A]] =
      for {
        i <- Gen.anyInt
        a <- genA
        j <- Gen.anyInt
      } yield Qux(i,a,j)
  }

  final case class Wub(x: Long)
  object Wub {
    implicit val eqWub: Eq[Wub] = Eq.by(_.x)

    val gen: Gen[Random, Wub] =
      Gen.anyLong.map(Wub(_))
  }

  sealed trait Foo extends Product with Serializable
  object Foo {
    final case class Bar(i: Int, s: String) extends Foo
    final case class Bam(w: Wub, d: Double) extends Foo
    final case class Baz(xs: List[String]) extends Foo
    // object Baz {
    //   implicit val decodeBaz: Decoder[Baz] =
    //     Decoder[List[String]].map(Baz(_))
    //   implicit val encodeBaz: Encoder[Baz] =
    //     Encoder.instance { case Baz(xs) =>
    //       Json.fromValues(xs.map(Json.fromString))
    //     }
    // }

    implicit val eqFoo: Eq[Foo] = Eq.fromUniversalEquals

    val gen: Gen[Random with Sized, Foo] =
      Gen.oneOf(
        Gen.crossN(Gen.anyInt, Gen.alphaNumericString) { Bar(_,_) },
        Gen.listOf(Gen.alphaNumericString).map { Baz(_) },
        Gen.crossN(Wub.gen, Gen.anyDouble) { Bam(_,_) }
      )
  }  
}
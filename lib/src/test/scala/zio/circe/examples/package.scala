package zio.circe

import zio.random._
import zio.test._

package examples {
  final case class Box[A](a: A)
  object Box {
    def gen[A](genA: Gen[Random, A]): Gen[Random, Box[A]] =
      genA.map(Box(_))
  }
  
  final case class Qux[A](i: Int, a: A, j: Int)
  object Qux {
    def gen[A](genA: Gen[Random, A]): Gen[Random, Qux[A]] =
      for {
        i <- Gen.anyInt
        a <- genA
        j <- Gen.anyInt
      } yield Qux(i,a,j)
  }

  final case class Wub(x: Long)
  object Wub {
    val gen: Gen[Random, Wub] =
      Gen.anyLong.map(Wub(_))
  }

  sealed trait Foo extends Product with Serializable
  object Foo {
    final case class Bar(i: Int, s: String) extends Foo
    final case class Bam(w: Wub, d: Double) extends Foo
    final case class Baz(xs: List[String]) extends Foo

    val gen: Gen[Random with Sized, Foo] =
      Gen.oneOf(
        Gen.crossN(Gen.anyInt, Gen.alphaNumericString) { Bar(_,_) },
        Gen.listOf(Gen.alphaNumericString).map { Baz(_) },
        Gen.crossN(Wub.gen, Gen.anyDouble) { Bam(_,_) }
      )
  }  
}
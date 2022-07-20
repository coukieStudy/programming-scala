import scala.annotation
import scala.annotation.tailrec
def reduceLeft[A,B](s:Seq[A])(f: A => B): Seq[B] = {
  @tailrec
  def rl(accum: Seq[B], s2: Seq[A]): Seq[B] = s2 match {
    case head +: tail => rl(f(head) +: accum, tail)
    case _ => accum
  }
  rl(Seq.empty[B], s)
}

def reduceRight[A,B](s:Seq[A])(f:A => B):Seq[B] = s match {
  case head +: tail => f(head) +: reduceRight(tail)(f)
  case _ => Seq.empty[B]
}

val list = List(1, 2, 3, 4, 5, 6)

reduceLeft(list)(i => 2*i)
reduceRight(list)(i => 2*i)

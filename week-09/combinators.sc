object Combinators1 {
  // 일반적이 map의 형태. 리스트에 임의의 함수를 적용한다
  def map[A,B](list: List[A])(f: (A) => B): List[B] = list map f
}

object Combinators {
  // 인자의 순서가 바뀌었다
  def map[A,B](f: (A) => B)(list: List[A]):List[B] = list map f
}

val intToString = (i:Int) => s"N=$i"

val flist = Combinators.map(intToString) _ // intToString을 함수에 적용 가능하게 끌어올림

flist(List(1, 2, 3, 4))

# for 내장
## 복습
```scala
object RemoveBlanks {  
  
  def apply(path: String, compressWhiteSpace: Boolean = false): Seq[String] =  
    for {  
      line <- scala.io.Source.fromFile(path).getLines.toSeq  
      if line.matches("""^\s*$""") == false  
  line2 = if (compressWhiteSpace) line replaceAll ("\\s+", " ")  
      else line  
    } yield line2  
  
  def main(args: Array[String]) = for {  
    path2 <- args  
    (compress, path) = if (path2 startsWith "-") (true, path2.substring(1))  
    else (false, path2)  
    line <- apply(path, compress)  
  } println(line)  
}
```
## for 내장: 내부 동작
- for 내장은 실제로는 foreach, map, flatMap, withFliter를 호출하는 syntax sugar
	- withFilter: filter와 달리 바로 뒤에 체이닝된 메서드와 결합하여 원소 하나씩마다 연산을 처리한다. (https://www.baeldung.com/scala/filter-vs-withfilter)
- 왜 쓰는가? 가독성
- for <-> foreach
```scala
val states = List("Alabama", "Alaska", "Virginia", "Wyoming")

for {
  s <- states
} println(s)

states foreach println
```
- for <-> map
```scala
val states = List("Alabama", "Alaska", "Virginia", "Wyoming")

for {
  s <- states
} yield s.toUpperCase

states map (_.toUpperCase)
```
- for <-> flatMap
```scala
val states = List("Alabama", "Alaska", "Virginia", "Wyoming")

for {
  s <- states
  c <- s
} yield s"$c-${c.toUpper}"

states flatMap(_.toSeq map (c => s"$c-${c.toUpper}"))
```
- for <-> withFliter
```scala
val states = List("Alabama", "Alaska", "Virginia", "Wyoming")

for {
  s <- states
  c <- s
  if c.isLower
} yield s"$c-${c.toUpper}"

states flatMap(_.toSeq withFilter (_.isLower) map (c => s"$c-${c.toUpper}"))
```
- 지역변수가 들어간 경우
```scala
val states = List("Alabama", "Alaska", "Virginia", "Wyoming")

for {
  s <- states
  c <- s
  if c.isLower
  c2 = s"$c-${c.toUpper} "
} yield c2

states flatMap(_.toSeq withFilter (_.isLower) map { c => 
  val c2 = s"$c-${c.toUpper} "
  c2
})
```
## for 내장의 변환 규칙
- 제너레이터 식 -> 패턴 식
```scala
// pat <- expr
pat <- expr.withFilter { case pat => true; case _ => false }
```
- 제너레이터가 하나만 있고 끝에 yield가 있는 for 내장 -> map
```scala
// for ( pat <- expr1 ) yield expr2
expr1 map { case pat => expr2 }
```
- yield가 없고 부수 효과만 수행하는 for 루프 -> foreach
```scala
// for ( pat <- expr1 ) expr2
expr1 foreach { case pat => expr2 }
```
- 제너레이터가 하나 이상인 for 내장 -> flatMap
```scala
// for ( pat <- expr1; pat2 <- expr2; ... ) yield exprN
expr1 flatMap { case pat1 => for (pat2 <- expr2 ...) yield exprN }
// 변환을 반복 적용하여 완전한 메서드 호출로 바꿀 수 있다
```
- 제너레이터가 하나 이상인 for 루프 (yield 가 없는) -> flatMap
```scala
// for ( pat <- expr1; pat2 <- expr2; ... ) exprN
expr1 foreach { case pat1 => for (pat2 <- expr2 ...) exprN }
// 변환을 반복 적용하여 완전한 메서드 호출로 바꿀 수 있다
```
- 제너레이터 다음에 가드가 오는 경우 -> withFilter
```scala
// pat1 <- expr1 if guard
pat1 <- expr1 withFilter ((arg1, arg2, ...) => guard)
```
- 제너레이터 다음에 변수 정의가 오는 경우 (복잡함)
```scala
// pat1 <- expr1; pat2 = expr2
(pat1, pat2) <- for {
  x1 @ pat1 <- expr1
} yield {
  val x2 @ pat2 = expr2
  (x1, x2)
}
```
- `x1 @ pat1 <- expr1`: expr1이 pat1에 일치할 경우, 일치하는 전체 값을 x1에 할당하라
- 그래서 이짓을 왜 함?
	- 스칼라 컴파일러는 for 내장을 map, flatmap, withFilter, foreach 등으로 변환한다.
	- 따라서 for 내장에서 런타임 에러가 발생했을 때, 에러 메시지에 map, flatmap, withFilter, foreach 등이 언급될 수 있다.
	- 만약 변환을 모른다면 디버깅에 어려움을 겪을 수 있다.
## Option과 다른 컨테이너 타입
- foreach, map, flatmap, withFilter를 구현했다면 컬렉션 타입이 아니어도 for 내장에서 사용할 수 있다.
### 컨테이너로서의 Option
- Option은 이진 컨테이너이다. 원소를 가지고 있거나 그렇지 않거나.
- Option에 4가지 메서드가 다음과 같이 정의되어 있다.
```scala
sealed abstract class Option[+A] { self => {
  ...
  def isEmpty: boolean

  final def foreach[U](f: A => U): Unit =
    if (!isEmpty) f(this.get)

  final def map[B](f: A => B): Option[B] = 
    if (isEmpty) None else Some(f(this.get))

  final def flatMap[B](f: A => Option[B]): Option[B] =
    if (isEmpty) None else f(this.get)

  final def filter(p: A => Boolean): Option[A] =
    if (isEmpty || p(this.get)) this else None

  final def withFilter(p: A => Boolean): WithFilter = new WithFilter(p)

  class WithFilter(p: A=> Boolean) {
    def map[B](f: A => B): Option[B] = self filter p map f
    def flatMap[B](f: A => Option[B]): Option[B] = self filter p flatMap f
    def foreach[U](f: A => U): Unit = self filter p foreach f
    def withFilter(q: A => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
  }
}
```
- 이와 같은 정의로 인해 Option은 비어 있지 않은 경우에만 함수 인자를 적용한다.
	- 이를 이용해 분산 처리를 수행할 수 있다. (오류는 무시하는 방식으로)
- 다음 식을 변환해보자.
```
val results: Seq[Option[Int]] = Vector(Some(10), None, Some(20))

val results2 = for {
  Some(i) <- results
} yield (2 * i)

// 변환 단계 #1: 제너레이터 -> withFilter
val results2b = for {
  Some(i) <- results withFilter {
    case Some(i) => true
    case None => false
  }
} yield (2 * i)

// 변환 단계 #2: yield -> map
val results2c = for {
  Some(i) <- results withFilter {
    case Some(i) => true
    case None => false
  } map {
    case Some(i) => (2 * i)	// 실제로는 컴파일 경고가 발생한다, case문이 exhaustive하지 않으므로.
  }
} 
```
- 다만 Option을 사용하면 None이 발생한 이유에 대한 정보는 얻을 수 없다.
```scala
def positive(i: Int): Option[Int] = if (i > 0) Some(i) else None

for {
  i1 <- positive(5).right
  i2 <- positive(10 * i1).right
  i3 <- positive(25 * i2).right
  i4 <- positive(2 * i3).right
} yield (i1 + i2 + i3 + i4)
// 반환: Option[Int] = Right(3805)

for {
  i1 <- positive(5).right
  i2 <- positive(-1 * i1).right
  i3 <- positive(25 * i2).right
  i4 <- positive(-2 * i3).right
} yield (i1 + i2 + i3 + i4)
// 반환: Option[Int] = None
```
### Either: Option의 논리적 확장
- Either는 두 가지 중 한 가지만 담을 수 있는 컨테이너다. 우리가 Either가 가질 수 있는 두 가지 값을 정의하면 Either는 그중 하나만 가지게 된다.
- Either의 타입은 `Either[+A, +B]`이다. A 타입 값과 B 타입 값 중 하나만 가질 수 있다는 뜻이다.
	- `+A`란 타입이 매개변수에 대해 공변적이라는 것을 의미한다. String과 Int가 Any의 서브타입이므로 `Either[String, Int]`가 `Either[Any, Any]`의 서브타입이라는 뜻이다.
- Either는 Left[A]와 Right[B]의 두 서브클래스를 가진다. 이 둘을 이용해 실제로 어떤 값을 가지고 있는지 나타낼 수 있다.
```scala
val l: Either[String, Int] = Left("boo")

val r: Either[String, Int] = Right(12)
```
- Either를 이용해 정상값과 오류를 표시하는 값 중 하나를 담을 수 있다. 이를 통해 연산 중간에 발생한 오류를 볼 수 있도록 구현할 수 있다.
```scala
def positive(i: Int): Either[String,Int] = 
  if (i > 0) Right(i) else Left(s"nonpositive number $i")

for {
  i1 <- positive(5).right
  i2 <- positive(10 * i1).right
  i3 <- positive(25 * i2).right
  i4 <- positive(2 * i3).right
} yield (i1 + i2 + i3 + i4)
// 반환: scala.util.Either[String,Int] = Right(3805)

for {
  i1 <- positive(5).right
  i2 <- positive(-1 * i1).right
  i3 <- positive(25 * i2).right
  i4 <- positive(-2 * i3).right
} yield (i1 + i2 + i3 + i4)
// 반환: scala.util.Either[String,Int] = Left(nonpositive number -5)
```
- 어떻게 이것이 가능할까?
	- Either에는 map 등이 정의되어 있지 않다. Left일 경우와 Right일 경우에 각각 호출되는 메서드가 달라야 하기 때문이다.
	- 따라서 map 같은 콤비네이터 메서드를 호출할 수 있는 "프로젝션"을 따로 둔다.
	- Either 인스턴스는 left, right 필드를 가지고 있고, 이것들이 각각 LeftProjection과 RightProjection이다.
	- LeftProjection과 RightProjection은 둘 다 Left와 Right를 모두 가질 수 있다. 하지만 LeftProjection은 Left를, RightProjection은 Right를 가지고 있을 때만 메서드에 인자로 들어온 함수를 호출한다.
- 그냥 예외를 던지게 하면 안 되는가?
```scala
def addInts(s1: String, s2: String): Int = s1.toInt + s2.toInt

for {
  i <- 1 to 3
  j <- 1 to i
} println(s"$i+$j = ${addInts(i.toString, j.toString)}")
```
	- No. 예외를 던지는 것은 참조 투명성을 해친다.
	- 참조 투명성: 특정 표현식을 그것의 평가 결과로 대체할 수 있는가?
	- 예외를 던지는 함수가 있다면 당연히 그 함수를 값으로 대체할 수 없다.
	- 참조 투명성이 중요한 이유는 표현식이 실행 문맥과 상관없이 항상 같은 결과를 보장하기 때문이다 (https://sookocheff.com/post/fp/why-functional-programming/). 가령 함수가 예외를 던지는 경우 try-catch 문맥 하에서 그 함수가 실행되면 결과가 달라질 수 있다.
	- 자바의 checked exception이 이런 문제를 해결해줄 수 있으나, 실제로는 여러 문제가 많아 (ex. 트랜잭션 롤백이 안됨) 잘 사용하지 않는다  
	- Either를 사용하면 참조 투명성을 지키면서 예외를 확인할 수 있다.
```scala
def addInts2(s1: String, s2: String): Either[NumberFormatException,Int] =
  try {
    Right(s1.toInt + s2.toInt)
  } catch {
    case nfe: NumberFormatException => Left(nfe)
  }
```
### Try: 할 수 있는 일이 없을 때
- Either로 예외를 처리하려면 try-catch 문을 써야 하는 복잡함이 있다. 또한 왼쪽과 오른쪽 중 어느 쪽이 성공이고 실패인지 이름만으론 불명확하다. Try는 바로 이 점을 해결해준다.
- Try는 Success와 Failure의 두 서브클래스가 있으며, Failure는 항상 Throwable을 포함한다. 따라서 Try는 매개변수가 하나만 필요하다.
- 사용법은 아래와 같다.
```scala
import scala.util.{ Try, Success, Filure }

def positive(i: Int): Try[Int] = Try {
  assert (i > 0, s"nonpositive number $i")
  i
}

for {
  i1 <- positive(5).right
  i2 <- positive(10 * i1).right
  i3 <- positive(25 * i2).right
  i4 <- positive(2 * i3).right
} yield (i1 + i2 + i3 + i4)
// 반환: scala.util.Try[Int] = Success(3805)

for {
  i1 <- positive(5).right
  i2 <- positive(-1 * i1).right
  i3 <- positive(25 * i2).right
  i4 <- positive(-2 * i3).right
} yield (i1 + i2 + i3 + i4)
// 반환: scala.util.Try[Int] = Failure(
// java.lang.AssertionError: assertion failed: nonpositive number -5)
```
### 스칼라제드의 Validation
- Try는 연산 처리 중 가장 처음 발생한 예외를 알 수 있게 하지만, 처리 과정에서 발생하는 모든 예외를 알려주지는 못한다.
- 웹에서 사용자가 제출한 양식을 검증하는 상황 등에서는 처리 과정에서 발생한 모든 예외가 필요할 수 있다.
- 외부 라이브러리인 스칼라제드의 Validation 클래스가 그러한 기능을 지원해준다.
- 그냥 그런갑다 하고 넘어가자.
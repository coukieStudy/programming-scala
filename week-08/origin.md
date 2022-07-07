### 5.2.8 암시적 인자 처리 규칙



1. 마지막 인자 목록에만 암시적 인자가 들어갈 수 있다.
2. implicit 키워드는 인자목록의 맨 처음에 와야 하며, 오직 한 번만 나타날 수 있다. 암시적 인자 다음에 비암시적 인자가 따라올 수 없다.
3. 인자 목록이 implicit 키워드로 시작하면, 그 인자 목록 안의 모든 인자가 암시적 인자가 된다.



```scala
def bad1(i: Int, implicit s: String) // 유일한 인자목록의 두 번째 인자에 implicit을 지정
def bad2(i: Int)(implicit s: String)(implicit d: Double) // 두 번째 인자가 implicit 인데, 세번째 인자도 implicit
def good1(i: Int)(implicit s: String, d: Double) // s, d는 모두 암시적 인자
```



### 5.3 암시적 변환

>  Map 을 초기화할 때 Map("one" -> 1, "two" ->2) 와 같은 문법을 사용할 수 있다. -> 는 어떻게 tuple 로 변환되는 것인가?

a -> b 를 (a, b) 로 변환해야 한다.

-> 라는 메서드를 정의해야한다. 하지만 튜플의 첫 원소가 될 가능성이 있는 모든 객체에 대해 정의가 필요하다. 따라서 이런 방법은 비효율적이다. 스칼라에서는 일종의 wrapper 클래스를 사용한다.

```scala
implicit final class ArrowAssoc[A] (val self: A) {
  def -> [B](y: B): Tuple[A, B] = Tuple2(self, y)
}
```

이제 동작 순서를 보면 다음과 같다.

1. 컴파일러는 "one" -> 1을 보고 String에 대해 -> 를 호출하려는 사실을 발견한다
2. String 에는 -> 메서드가 없다. 따라서 범위 안에서 String에 대해 다른 타입으로 변환하면서 -> 라는 이름을 가진 암시적 변환 메서드를 찾는다
3. ArrowAssoc 을 발견한다
4. ArrowAssoc 에 "one" 이라는 문자열을 넘겨서 새 ArrowAssoc 객체를 만든다.
5. 그 후 원래 식의 -> 1 부분을 처리하고, 전체 식의 타입이 Map 생성자가 요구하는 튜플 인스턴스와 맞아떨어지는지 검사한다.



암시적 변환의 고려 대상이 되기 위해서는 반드시 implicit 키워드와 함께 선언되어야 하며 인자를 하나만 받는 생성자가 있는 클래스이거나, 인자를 하나만 받는 메서드여야 한다. 컴파일러가 암시적 변환 메서드를 찾을 때 사용하는 규칙은 다음과 같다.

1. 객체와 메서드 조합이 타입 변환 없이도 통과하면 타입 변환을 시도하지 않는다
2. implicit 키워드가 붙은 클래스와 메서드만 고려한다
3. 현재 범위 안에 있는 암시적 클래스와 메서드만 고려한다. (현재 범위 = 실행 범위 안에 정의되어 있거나, 암시적 변환이 포함된 객체 등이 임포트 되어있다). 동반 객체 안에 정의된 암시적 메서드도 고려된다.
4. 암시 메서드를 둘 이상 조합해서, 한 타입을 중간 타입으로 변환한 뒤 다른 암시적 변환으로 최종 타입으로 변환하는 시도는 하지 않는다.
5. 유일하고 모호하지 않을 가능성이 있어야 변환을 수행한다.



```scala
import scala.language.implicitConversions

case class Foo(s: String)
object Foo {
  implicit def fromString(s: String): Foo = Foo(s)
}

//implicit def overridingConversion(s: String): Foo = Foo("Boo: "+s)

class O {
  def m1(foo: Foo) = println(foo)
  def m(s: String) = m1(s)
}

```

0.m 은 내부적으로 0.m1 을 부르지만, 0.m1 은 Foo 타입의 인자를 받는다. 컴파일러는 0의 범위 안에 명시적으로 임포트한 적은 없지만, Foo.fronString 변환 메서드를 찾을 수 있다.

하지만 다른 Foo 변환이 범위에 있다면, 그 변환이 Foo.fromString보다 우선적으로 실행된다.



### 5.4 타입 클래스 패턴

타입 클래스를 사용하면 자바의 Object 처럼 임의의 동작을 추가하기 위해 모든 것을 추상화하려는 유혹을 피할 수 있다. 자바의 Object.toString() 과 비슷하지만, 훨씬 유용한 toJson() 과 같은 메서드를 정의할 수도 있다.

```scala
case class Address(street: String, city: String)
case class Person(name: String, address: Address)

trait ToJSON {
  def toJSON(level: Int = 0): String

  val INDENTATION = "  "
  def indentation(level: Int = 0): (String,String) = 
    (INDENTATION * level, INDENTATION * (level+1))
}

implicit class AddressToJSON(address: Address) extends ToJSON {
  def toJSON(level: Int = 0): String = {
    val (outdent, indent) = indentation(level)
    s"""{
      |${indent}"street": "${address.street}", 
      |${indent}"city":   "${address.city}"
      |$outdent}""".stripMargin
  }
}

implicit class PersonToJSON(person: Person) extends ToJSON {
  def toJSON(level: Int = 0): String = {
    val (outdent, indent) = indentation(level)
    s"""{
      |${indent}"name":    "${person.name}", 
      |${indent}"address": ${person.address.toJSON(level + 1)} 
      |$outdent}""".stripMargin
  }
}

val a = Address("1 Scala Lane", "Anytown")
val p = Person("Buck Trends", a)

println(a.toJSON())
// {
//   "street": "1 Scala Lane",
//   "city:": "Anytown"
// }
println()
println(p.toJSON())
```



Address, Person 이 직접 ToJSON을 상속하지 않아도 toJson() 메서드를 사용할 수 있게 된다.



### 5.5 암시와 관련된 기술적 문제

* 암시적 변환에서 버그가 발생하면 찾기가 어렵다. 
* 암시적 변환은 래퍼 타입으로 인한 간접 계층을 한번 더 거쳐야 하기 때문에 실행 시점에 추가적인 비용이 든다. 
* 암시적 변환 메서드의 반환 타입을 명시하지 않고 타입 추론에 의지하면 암시를 사용할 때의 문제점이 커지므로, 반드시 반환 타입을 명시해야 한다.

암시적 값을 모두 implicits 라는 패키지나 Implicits 라는 객체에 넣는 것이 관례이다. 이런 방식을 사용하면 사용자가 'implicit' 이라는 단어를 임포트문에서 보고, 다른 암시를 사용중임을 알 수 있다.



### 5.6 암시 해결 규칙

다음 우선순위에 따라 암시 규칙이 정해진다.

1. 현재 범위로 임포트한 암시적 값을 검색한다
2. 타입이 호환되는 모든 암시적 값(같은 코드블록이나 같은 타입 안에 있거나 동반 객체에 있거나, 부모 타입 안에 정의 된 값) 을 검색한다.

타입이 호환될 수 있는 경우가 여러가지가 있을 때에는 가장 구체적인 타입이 선택된다. 구체적인 타입이 같아서 둘 이상의 암시적 값이 모호하다면 컴파일러 오류가 발생한다.



### 5.7 스칼라가 제공하는 기본 암시

스칼라 2.11 라이브러리 소스 코드에는 300개 이상의 암시 메서드, 값, 타입이 있다. 그 중 대부분은 한 타입을 다른 타입으로 변환하기 위해 사용되는 암시 메서드이다. Int를 다른 타입으로 변환하기 위한 메서드의 예시이다.

``` scala
implicit def int2long(x: Int): Long = x.toLong
implicit def int2float(x: Int): Float = x.toFloat;
implicit def int2double(x: Int): Double = x.toDouble;
```

 Char, Short, Long, Float, Double 등도 마찬가지로 이러한 암시적 변환을 가지고 있다.

자바 타입의 원소들과 변환하기 위한 메서드들도 존재한다. (Predef)

```scala
implicit def byte2Byte(x: Byte) = java.lang.Byte.valueOf(x)
implicit def Byte2byte(x: java.lang.Byte): Byte = x.byteValue
// 다른 타입에 대해서도 비슷한 함수들이 존재한다.
```

자바 Collection 과 스칼라 컬렉션을 처리하기 위한 데코레이션들도 존재한다. (Scala.collection.convert)

```scala
implicit def asJavaIteratorConverter[A](i: Iterator[A]): AsJava[java.util.Iterator[A]] = ...
implicit def asScalaIteratorConverter[A](i: java.util.Iterator[A]): AsScala[Iterator[A]] = ...
```

이 외에도 다양한 기본 패키지의 암시적 변환들이 있다.

# 4장 패턴 매칭

## 가변 인자 목록과 일치시키기

SQL로 작업하는 도구를 작성하는 중이고, 아래 SQL 조건절을 표현하는 케이스 클래스를 원한다고 가정하자.

```sql
WHERE foo IN (val1, val2, ....)
```

```scala
// WHERE 절에서 사용할 연산자들
object Op extends Enumeration { //열거형
  type Op = Value

  val EQ   = Value("=")
  val NE   = Value("!=")
  val LTGT = Value("<>")
  val LT   = Value("<")
  val LE   = Value("<=")
  val GT   = Value(">")
  val GE   = Value(">=")
}
import Op._

// SQL의 'WHERE x op value'절을 표현한다.
// op는 =, !=, <>, <, <=, >, or >= 등의 비교 연산자다.
case class WhereOp[T](columnName: String, op: Op, value: T)

// SQL의 'WHERE x IN (a, b, c, ...)'절을 표현한다.
case class WhereIn[T](columnName: String, val1: T, vals: T*)

val wheres = Seq(                                                    // <4>
  WhereIn("state", "IL", "CA", "VA"),
  WhereOp("state", EQ, "IL"),
  WhereOp("name", EQ, "Buck Trends"),
  WhereOp("age", GT, 29))

for (where <- wheres) {
  where match {
    case WhereIn(col, val1, vals @ _*) =>                            // <5>
      val valStr = (val1 +: vals).mkString(", ")
      println (s"WHERE $col IN ($valStr)")
    case WhereOp(col, op, value) => println (s"WHERE $col $op $value")
    case _ => println (s"ERROR: Unknown expression: $where")
  }
}
```

- WhereIn, WhereOp case class들을 패턴 매칭을 통해서 처리하는 모습을 볼 수 있다.
- @ _*: @이는 왜 필요한지..?
    - [https://dzone.com/articles/7-uses-of-underscore-in-scala](https://dzone.com/articles/7-uses-of-underscore-in-scala)

## 정규 표현식과 일치시키기

Scala는 java의 정규표현식을 감싸서 사용한다.

```scala
val BookExtractorRE = """Book: title=([^,]+),\s+author=(.+)""".r     // <1>
val MagazineExtractorRE = """Magazine: title=([^,]+),\s+issue=(.+)""".r

val catalog = Seq(
  "Book: title=Programming Scala Second Edition, author=Dean Wampler",
  "Magazine: title=The New Yorker, issue=January 2014",
  "Unknown: text=Who put this here??"
)

for (item <- catalog) {
  item match {
    case BookExtractorRE(title, author) =>                           // <2>
      println(s"""Book "$title", written by $author""")
    case MagazineExtractorRE(title, issue) =>
      println(s"""Magazine "$title", issue $issue""")
    case entry => println(s"Unrecognized entry: $entry")
  }
}
```

- (1) : capture group(정규 표현식 안에 괄호로 둘러싸인 부분)이 두 개 존재.
    - 첫번째는 제목을 잡아내고, 두번재는 작가를 잡아낸다.
    - 끝에 r 메서드를 문자열에 대해 호출하면 scala.util.matching.Regex 타입을 만들어낸다.
- 정규 표현식 객체를 케이스 클래스 객체처럼 사용해서 각 capture group이 잡아낸 문자열를 변수에 대입

## 케이스 절의 변수 바인딩에 대해 더 살펴보기

객체에서 값을 뽑아내고 싶지만, 객체 자체에도 변수를 대입하고 싶은 경우가 있다.

```scala
case class Address(street: String, city: String, country: String)
case class Person(name: String, age: Int, address: Address)

val alice   = Person("Alice",   25, Address("1 Scala Lane", "Chicago", "USA"))
val bob     = Person("Bob",     29, Address("2 Java Ave.",  "Miami",   "USA"))
val charlie = Person("Charlie", 32, Address("3 Python Ct.", "Boston",  "USA"))

for (person <- Seq(alice, bob, charlie)) {
  person match {
    case p @ Person("Alice", 25, address) => println(s"Hi Alice! $p")
    case p @ Person("Bob", 29, a @ Address(street, city, country)) => 
      println(s"Hi ${p.name}! age ${p.age}, in ${a.city}")
    case p @ Person(name, age, _) => 
      println(s"Who are you, $age year-old person named $name? $p")
  }
}
```

- p @ : 전체 person 인스턴스를 p에 대입하면, a @ 부분도 Address 객체를 대입한다.
- 인스턴스에서 필드를 추출할 필요가 없다면, p: Person => 이라고 쓰면 된다.

## 타입 일치에 대해 더 살펴보기

List[Double], List[String]을 구분하기 위한 시도

```scala
for {
  x <- Seq(List(5.5,5.6,5.7), List("a", "b")) 
} yield (x match {
  case seqd: Seq[Double] => ("seq double", seqd)
  case seqs: Seq[String] => ("seq string", seqs)
  case _                 => ("unknown!", x)
})
```

- JVM의 **`Generic Type erasure`** 때문에 실행시점에 List[Double], List[String]을 구분할 수 없다.
- Java 5 이전 코드와의 하위 호환성을 유지하기 위해 JVM 바이트 코드에는 List타입과 같은 Generic 타입의 인스턴스에 사용했던 타입 매개변수 정보가 들어 있지 않다.
- yield란? [https://knight76.tistory.com/entry/scala-for-%EB%AC%B8-yield](https://knight76.tistory.com/entry/scala-for-%EB%AC%B8-yield)

→ 결과적으로 위 코드는 seqd case만 타게 된다.

## 봉인된 클래스 계층과 매치의 완전성

HTTP에서 허용하는 메시지 타입이나 '메서드'를 표현하는 코드

```scala
sealed abstract class HttpMethod() {                                 // <1>
    def body: String                                                 // <2>
    def bodyLength = body.length
}

case class Connect(body: String) extends HttpMethod                  // <3>
case class Delete (body: String) extends HttpMethod
case class Get    (body: String) extends HttpMethod
case class Head   (body: String) extends HttpMethod
case class Options(body: String) extends HttpMethod
case class Post   (body: String) extends HttpMethod
case class Put    (body: String) extends HttpMethod
case class Trace  (body: String) extends HttpMethod

def handle (method: HttpMethod) = method match {                     // <4>
  case Connect (body) => s"connect: (length: ${method.bodyLength}) $body"
  case Delete  (body) => s"delete:  (length: ${method.bodyLength}) $body"
  case Get     (body) => s"get:     (length: ${method.bodyLength}) $body"
  case Head    (body) => s"head:    (length: ${method.bodyLength}) $body"
  case Options (body) => s"options: (length: ${method.bodyLength}) $body"
  case Post    (body) => s"post:    (length: ${method.bodyLength}) $body"
  case Put     (body) => s"put:     (length: ${method.bodyLength}) $body"
  case Trace   (body) => s"trace:   (length: ${method.bodyLength}) $body"
}

val methods = Seq(
  Connect("connect body..."),
  Delete ("delete body..."),
  Get    ("get body..."),
  Head   ("head body..."),
  Options("options body..."),
  Post   ("post body..."),
  Put    ("put body..."),
  Trace  ("trace body..."))

methods foreach (method => println(handle(method)))
```

- sealed class: 클래스의 sub class가 부모 클래스와 동일한 파일에 위치하도록 한다.
- 변경된 가능성이 높은 경우에는 타입 계층을 봉인하지 않아야 한다. 대신 다형성 메서드 등의 전통적인 객체지향 상속 원칙에 의존해야 한다.

→ 패턴 매치는 클래스별로 어떤 동작을 수행할지에 대해 match 문 안에서 볼 수 있고 어떤 경우에 처리하는지 명확히 알 수 있지만, 다형성을 활용하는 경우 여러 클래스에 있는 오버라이딩한 메서드로 그런 동작에 대한 표현이 분산

- 참조 투명성: [https://knight76.tistory.com/entry/scala-%EC%B0%B8%EC%A1%B0-%ED%88%AC%EB%AA%85%EC%84%B1referential-transparency%EA%B3%BC-%EB%B6%80%EC%9E%91%EC%9A%A9side-effect](https://knight76.tistory.com/entry/scala-%EC%B0%B8%EC%A1%B0-%ED%88%AC%EB%AA%85%EC%84%B1referential-transparency%EA%B3%BC-%EB%B6%80%EC%9E%91%EC%9A%A9side-effect)

## 패턴 매칭의 다른 사용법

case 절이 아닌 경우에도 사용 가능

```scala
val head +: tail = List(1,2,3)
//head: Int = 1
//tail: List[Int] = List(2,3)

val head1 +: head2 +: tail = Vector(1,2,3)
//head1: Int = 1
//head2: Int = 2
//tail: Vector[Int] = Vector(2,3)

val p = Person("Dean", 29, Address("1"))
if (p == Person("Dean", 29, Address("1"))) "yes" else "no"
```

```scala
case class Address(street: String, city: String, country: String)
case class Person(name: String, age: Int)

val as = Seq(
  Address("1 Scala Lane", "Anytown", "USA"),
  Address("2 Clojure Lane", "Othertown", "USA"))
val ps = Seq(
  Person("Buck Trends", 29),
  Person("Clo Jure", 28))

val pas = ps zip as

// 보기 좋지 않은 예:
pas map { tup =>
  val Person(name, age) = tup._1
  val Address(street, city, country) = tup._2
  s"$name (age: $age) lives at $street, $city, in $country"
}

// 보기 좋은 예:
pas map {
  case (Person(name, age), Address(street, city, country)) =>
    s"$name (age: $age) lives at $street, $city, in $country"
}
```

- zip: [https://knight76.tistory.com/entry/scala-zip-unzip-%EC%98%88%EC%8B%9C](https://knight76.tistory.com/entry/scala-zip-unzip-%EC%98%88%EC%8B%9C)

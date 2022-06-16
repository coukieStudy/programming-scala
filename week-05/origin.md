# 패턴 매칭
## 단순 매치
아래와 같은 C-style의 case문을 사용할 수 있다.
```scala
val bools = Seq(true, false)  
for (bool <- bools) {  
  bool match {  
    case true => println("Got heads")  
    case false => println("Got tails")  
  }  
}
```
이와 같은 용법은 단순 if문으로 대신할 수 있다.
## 매치 내의 값, 변수, 타입
특정 값이나 타입으로 매칭을 할 수 있다.
```scala
for {  
  x <- Seq(1, 2, 2.7, "one", "two", 'four) //  
} {  
  val str = x match { //  
  case 1 => "int 1" //  
  case i: Int => "other int: "+i //  
  case d: Double => "a double: "+x //  
  case "one" => "string one" //  
  case s: String => "other string: "+s //  
  case unexpected => "unexpected value: " + unexpected //  
  }  
  println(str) //  
}
```
- match 문의 리턴 타입은 컴파일러가 모든 case문의 가장 가까운 슈퍼타입으로 추론해준다.
- 위와 같이 커버해야 하는 값이 넓은 경우 default 문을 포함시키는 것이 좋다. 단 부분함수를 작성할 때는 그럴 필요 없다.
- 매치는 순서대로 이루어진다. 따라서 위쪽에 더 구체적인 조건을, 아래쪽에 덜 구체적인 조건을 걸어야 한다. 즉 default문은 가장 아래에 와야 한다. 순서가 잘못된 경우 컴파일러가 알려준다.
- 변수 `i`, `d`, `s` 를 사용하지 않는다면 대신 플레이스홀더 `_`를 사용할 수 있다.
- 특정 변수에 매칭하고 싶으면 변수를 backtick으로 감싸야 한다 (ex. `` `y` ``). 그냥 쓰면 그건 default문이 된다.
- case문은 `|`(or)를 지원한다.
## 시퀀스에 일치시키기
- `Seq`는 `List`와 `Vector`와 같이 순서 확정적인 컬렉션의 부모 타입이다.
```scala
val nonEmptySeq = Seq(1, 2, 3, 4, 5) //  
val emptySeq = Seq.empty[Int]  
val nonEmptyList = List(1, 2, 3, 4, 5) //  
val emptyList = Nil  
val nonEmptyVector = Vector(1, 2, 3, 4, 5) //  
val emptyVector = Vector.empty[Int]  
val nonEmptyMap = Map("one" -> 1, "two" -> 2, "three" -> 3) //  
val emptyMap = Map.empty[String,Int]  
def seqToString[T](seq: Seq[T]): String = seq match { //  
  case head +: tail => s"$head +: " + seqToString(tail) //  
  case Nil => "Nil" //  
}  
for (seq <- Seq( //  
  nonEmptySeq, emptySeq, nonEmptyList, emptyList,  
  nonEmptyVector, emptyVector, nonEmptyMap.toSeq, emptyMap.toSeq)) {  
  println(seqToString(seq))  
}
```
- `+:`는 시퀀스의 cons operator이다. 리스트의 `::`와 같은 역할을 한다.
- `Nil`은 빈 리스트를 가리킨다. null과 다르다.
- `case head +: tail` 문은 시퀀스를 head와 tail로 분리시킨다. 구체적인 동작 원리는 뒤에서.
- output은 다음과 같다.
```
1 +: 2 +: 3 +: 4 +: 5 +: Nil 
Nil 
1 +: 2 +: 3 +: 4 +: 5 +: Nil 
Nil 
1 +: 2 +: 3 +: 4 +: 5 +: Nil 
Nil 
(one,1) +: (two,2) +: (three,3) +: Nil 
Nil
```
## 튜플에 일치시키기
```scala
val langs = Seq(  
  ("Scala", "Martin", "Odersky"),  
  ("Clojure", "Rich", "Hickey"),  
  ("Lisp", "John", "McCarthy"))  
for (tuple <- langs) {  
  tuple match {  
    case ("Scala", _, _) => println("Found Scala") //  
  case (lang, first, last) => //  
  println(s"Found other language: $lang ($first, $last)")  
  }  
}
```
## 케이스 절의 가드
다음과 같이 case문에 if 문을 넣을 수 있다.
```scala
for (i <- Seq(1,2,3,4)) {  
  i match {  
    case _ if i%2 == 0 => println(s"even: $i") //  
  case _ => println(s"odd: $i") //  
  }  
}
```
## 케이스 클래스에 일치시키기
다음과 같이 케이스 클래스에 매칭할 수 있다. 심지어 nested type도 매칭이 가능하다.
```scala
// Simplistic address type. Using all strings is questionable, too.  
case class Address(street: String, city: String, country: String)  
case class Person(name: String, age: Int, address: Address)  
val alice = Person("Alice", 25, Address("1 Scala Lane", "Chicago", "USA"))  
val bob = Person("Bob", 29, Address("2 Java Ave.", "Miami", "USA"))  
val charlie = Person("Charlie", 32, Address("3 Python Ct.", "Boston", "USA"))  
for (person <- Seq(alice, bob, charlie)) {  
  person match {  
    case Person("Alice", 25, Address(_, "Chicago", _)) => println("Hi Alice!")  
    case Person("Bob", 29, Address("2 Java Ave.", "Miami", "USA")) =>  
      println("Hi Bob!")  
    case Person(name, age, _) =>  
      println(s"Who are you, $age year-old person named $name?")  
  }  
}
```
아래와 같이 튜플도 nested 매핑이 가능하다.
```scala
val itemsCosts = Seq(("Pencil", 0.52), ("Paper", 1.35), ("Notebook", 2.43))  
val itemsCostsIndices = itemsCosts.zipWithIndex  
for (itemCostIndex <- itemsCostsIndices) {  
  itemCostIndex match {  
    case ((item, cost), index) => println(s"$index: $item costs $cost each")  
  }  
}
```
### Unapply 메서드
- 그래서 값을 어떻게 추출하는 거임?
- 이전에 배웠듯이 케이스 클래스는 팩토리 메서드 `apply`를 가진 컴패니언 오브젝트를 가진다. `apply` 메서드는 인스턴스를 생성(construction)하기 위한 것이다. 이와는 대칭적으로 인스턴스를 분해(deconstruction)하기 위한 메서드인 `unapply`가 존재하며 이 메서드가 패턴 매칭시에 호출된다.
```scala
person match { 
  case Person("Alice", 25, Address(_, "Chicago", _)) => ...
  ... 
}
```
- 위와 같은 패턴 매칭 구문이 실행될 때 스칼라는 `Person.unapply(...)`와 `Address.unapply(...)`를 찾아 호출한다.
```scala
object Person {  
  def apply(name: String, age: Int, address: Address) =  
    new Person(name, age, address)  
  def unapply(p: Person): Option[Tuple3[String,Int,Address]] =  
    Some((p.name, p.age, p.address))  
  ...  
}
```
- `Person`은 3개의 값을 포함한다. 따라서 `Person.unapply(...)`는 원소가 3개인 튜플의 `Option`을 반환한다.
- 타입이 분명한데도 `Option`을 반환하는 이유? 특별한 경우 `None`을 반환할 수 있도록 함으로써 다음 case 구문이 사용될 수 있게 하기 위해.
- 특정 필드 값을 숨길 수도 있다 (구현은 자유).
- `unapply` 메서드는 재귀적으로 호출되어 nested type을 매칭할 수 있다.
- 그러면 `+:`는 어떻게 작동한 걸까?
- 스칼라 라이브러리에는 `+:`라는 이름의 싱글턴 오브젝트가 있다. 이 오브젝트에는 `unapply` 메서드 하나만 존재한다. 이걸 통해 패턴 매칭을 지원한다.
- 근데 그러면 이런 식으로 써야 하는 거 아님?
```scala
case +:(head, tail) => ...
```
- 스칼라는 중위 표기법(infix notation)을 지원한다. 따라서 위 표현은 아래와 동치이다.
```scala
case head +: tail => ...
```
- 마찬가지로 타입 선언에서도 다음과 같이 중위 표기법을 사용할 수 있다.
```scala
case class With[A,B](a: A, b: B) 
val with1: With[String,Int] = With("Foo", 1)
val with2: String With Int = With("Bar", 2)
```
- 근데 이건 안 된다.
```scala
val w = "one" With 2
```
### unapplySeq 메서드
- `unapplySeq` 메서드는 가변 길이의 시퀀스를 추출할 수 있게 해준다.
- `Seq`의 컴패니언 오브젝트는 `unapplySeq`만 구현하였고 `unapply`는 구현하지 않았다.
```scala
def apply[A](elems: A*): Seq[A] 
def unapplySeq[A](x: Seq[A]): Some[Seq[A]]
```
```scala
val nonEmptyList = List(1, 2, 3, 4, 5) //  
val emptyList = Nil  
val nonEmptyMap = Map("one" -> 1, "two" -> 2, "three" -> 3)  
// Process pairs  
def windows[T](seq: Seq[T]): String = seq match {  
  case Seq(head1, head2, _*) => //  
  s"($head1, $head2), " + windows(seq.tail) //  
  case Seq(head, _*) =>  
    s"($head, _), " + windows(seq.tail) //  
  case Nil => "Nil"  
}  
for (seq <- Seq(nonEmptyList, emptyList, nonEmptyMap.toSeq)) {  
  println(windows(seq))  
}
```
- 위와 같은 구문에서 `unapplySeq`가 사용된다.
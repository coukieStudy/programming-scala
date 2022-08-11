# 8장 스칼라 객체지향 프로그래밍

첫째, 함수형 프로그래밍이 최근의 문제를 풀기 위한 필수적인 기술

- 데이터 중심 프로그래밍
- 순수함수
- 불변성

[https://github.com/coukieStudy/programming-scala/blob/main/week-09/origin.md](https://github.com/coukieStudy/programming-scala/blob/main/week-09/origin.md)

둘째, Funtional Programming을 소규모 프로그래밍에 사용하고, OOP를 대규모 프로그래밍을 사용하라.

- FP(Funtional Programming): 알고리즘 구현/데이터 조작/버그 최소화/코드 양 줄임
- OOP: 조합 가능하고, 재사용 가능한 모듈을 설계할 수 있는 도구 제공

## 8.1 클래스와 객체의 기초

클래스: class 키워드

싱글턴: object 키워드

**Java vs Scala**

```java
public class JPerson {
  private String name;
  private int    age;

  public JPerson(String name, int age) {
    this.name = name;
    this.age  = age;
  }

  public void   setName(String name) { this.name = name; }
  public String getName()            { return this.name; }

  public void setAge(int age) { this.age = age;  }
  public int  getAge()        { return this.age; }
}
```

 

```scala
class Person(var name: String, var age: Int)
//var:변경 가능한
//val:변경 가능하지 않은

case class ImmutablePerson(name: String, age: Int)
```

**타입 소거**

JVM에서는 List와 Map과 같은 Higher kinded 타입에서 타입 매개변수의 타입을 소거한다.

→ JDK5에서 기존의 로 타입을 지원하기 위함

> Type erasure can be explained as the process of enforcing type constraints only at [**compile time**](https://www.baeldung.com/cs/compile-load-execution-time) and discarding the element type information at **runtime**.
> 

e.g. [https://www.baeldung.com/java-type-erasure](https://www.baeldung.com/java-type-erasure)

```scala
object C {
	def m(seq: Seq[Int]): Unit = println(s"Seq[Int]: $seq")
  def m(seq: Seq[String]): Unit = println(s"Seq[String]: $seq")
}

//error message: have same type after erasure

//타입 소거 우회하기
object C {
	implicit object IntMarker
  implicit object StringMarker

	def m(seq: Seq[Int])(implicit i: IntMarker.type): Unit = println(s"Seq[Int]: $seq")
  def m(seq: Seq[String])(implicit s: StringMarker.type): Unit = println(s"Seq[String]: $seq")
}
```

ref. [what is singleton type exactly?](https://stackoverflow.com/questions/33052086/what-is-a-singleton-type-exactly)

**Why Scala does NOT have “static” keyword? What is the main reason for this decision?**

> As we know, Scala does NOT have “static” keyword at all. This is the design decision done by Scala Team. The main reason to take this decision is to make Scala as a Pure Object-Oriented Language. “static” keyword means that we can access that class members without creating an object or without using an object. This is completely against with OOP principles. If a Language supports “static” keyword, then that Language is not a Pure Object-Oriented Language. For instance, as Java supports “static” keyword, it is NOT a Pure Object-Oriented Language. But Scala is a Pure Object-Oriented Language.
> 

ref. [https://www.geeksforgeeks.org/java-not-purely-object-oriented-language/](https://www.geeksforgeeks.org/java-not-purely-object-oriented-language/)

→ Question: 다들 Java에서 static keyword(e.g. method, field)를 사용하는 것에 대해 어떻게 생각하는지?

## 8.2 참조 타입과 값 타입

- Scala 값 타입 - *Char, Byte, Short, Int, Long, Float, Double*
    - AnyVal: 값 타입의 조상
    - 값 타입의 '인스턴스'는 힙에 만들어지지 않는다. 스택이나 레지스터에 저장된다.
    - boxing/unboxing을 최소화하여 성능을 최대화 - Java와 같이 primitive/Wrapper type이 나뉘어져 있지 않은 이유
    - [https://www.baeldung.com/scala/data-types](https://www.baeldung.com/scala/data-types)
- Scala 참조 타입 - 값 타입이 아닌 모든 타입
    - AnyRef: 참조 타입의 조상
- Unit이란? Java의 void와 비슷한 것이라 생각하면 됨(16.1에서 Nothing 등과 함께 자세히 알아봄)
    - [https://www.baeldung.com/scala/nil-null-nothing-unit-none](https://www.baeldung.com/scala/nil-null-nothing-unit-none)

## 8.3 값 클래스

확장 메서드(Externsion method)라고 부르는 타입 클래스를 구현하기 위해 Wrapper 타입을 추가하는 것이 일반적

```scala
class Dollar(val value: Float) extends AnyVal {
  override def toString = "$%.2f".format(value)
}

val benjamin = new Dollar(100)
```

```scala
class Meter(val value: Double) extends AnyVal {
  def +(m: Meter): Meter = new Meter(value + m.value)
}

val x = new Meter(3.4)
val y = new Meter(4.3)
val z = x + y
```

Meter를 사용할 때마다 생성하는 게 아니라 run time에는 primitive double을 사용한다.

Ref. [https://docs.scala-lang.org/overviews/core/value-classes.html](https://docs.scala-lang.org/overviews/core/value-classes.html)

→ Value class는 오직 범용 트레이트만 상속할 수 있다.

범용 트레이트란?

- Any를 상속한다
- 메서드만 정의한다
- 자체 초기화가 없다

```scala
//AS-IS
class USPhoneNumber(val s: String) extends AnyVal {

  override def toString = {
    val digs = digits(s)
    val areaCode  = digs.substring(0,3)
    val exchange  = digs.substring(3,6)
    val subnumber = digs.substring(6,10)  // 가입자 번호
    s"($areaCode) $exchange-$subnumber"
  }

  private def digits(str: String): String = str.replaceAll("""\D""", "") 
}

//TO-BE
trait M extends Any {
  def m = print("M ")
}

trait Digitizer extends Any with M {
  override def m = { print("Digitizer "); super.m }

  def digits(s: String): String = s.replaceAll("""\D""", "")
}

trait Formatter extends Any with M {   
  override def m = { print("Formatter "); super.m }

  def format(areaCode: String, exchange: String, subnumber: String): String =
    s"($areaCode) $exchange-$subnumber"
}

// 식이 2줄에 걸쳐 있음을 REPL에 알려주기 위해 'extends AnyVal'을 2줄로 나눠서 썼다.
class USPhoneNumber(val s: String) extends 
    AnyVal with Digitizer with Formatter{
  override def m = { print("USPhoneNumber "); super.m }
  
  override def toString = {
    val digs = digits(s)
    val areaCode = digs.substring(0,3)
    val exchange = digs.substring(3,6)
    val subnumber  = digs.substring(6,10)
    format(areaCode, exchange, subnumber)
  }
}
```

- 이로서 다른 형식의 formatter를 적용하고자 할 때는 다른 Formatter trait를 만들고 적용하면 된다.

## 8.4 부모 타입

자바와 동일하게 다중상속은 지원하지 않고, 단일 상속만 지원한다.

ref. [https://stackoverflow.com/questions/2064880/diamond-problem](https://stackoverflow.com/questions/2064880/diamond-problem)

## 8.5 스칼라에서의 생성자

Scala에는 Primary Constructor와 Secondary Constructor가 존재

- <1>이 secondary constructor이다.
- <1>에서는 Primary constrcutor를 호출하고 있다. 반드시 주 생성자나 다른 보조 생성자를 맨 첫번째 식으로 호출해야 한다.
- sceondary constructor는 소스 코드에서 자신보다 더 앞에 있는 생성자만 호출하도록 요구한다.

```scala
case class Address(street: String, city: String, state: String, zip: String) {

  def this(zip: String) =                                            
    this("[unknown]", Address.zipToCity(zip), Address.zipToState(zip), zip) //<1>
}

object Address {

  def zipToCity(zip: String)  = "Anytown"                            
  def zipToState(zip: String) = "CA"
}
```

```scala
case class Person2(
  name: String,
  age: Option[Int] = None,
  address: Option[Address] = None)

Person2("Buck Trends1")
// 결과: Person2 = Person2(Buck Trends1,None,None)

Person2("Buck Trends2", Some(20), Some(a1))
// 결과: Person2(Buck Trends2,Some(20),
//           Some(Address(1 Scala Lane,Anytown,CA,98765)))

Person2("Buck Trends3", Some(20))
// 결과: Person2(Buck Trends3,Some(20),None)

Person2("Buck Trends4", address = Some(a2))
// 결과: Person2(Buck Trends4,None,
//           Some(Address([unknown],Anytown,CA,98765)))
```

- Case class는 apply method를 기본적으로 정의하고 있고, Person2.apply() or Person2() 식으로 객체 생성 가능하다.
- Option은 이진 컨테이너

```scala
case class Person3(
  name: String,
  age: Option[Int] = None,
  address: Option[Address] = None)

object Person3 {

  // (생성자가 아니라) 일반 메서드를 오버로딩하기 때문에
  // 반환 타입 표기를 추가해야 한다. 여기서는 Person3다.
  def apply(name: String): Person3 = new Person3(name)

  def apply(name: String, age: Int): Person3 = new Person3(name, Some(age))

  def apply(name: String, age: Int, address: Address): Person3 =
    new Person3(name, Some(age), Some(address))

  def apply(name: String, address: Address): Person3 =
    new Person3(name, address = Some(address))
}

//Example
Person3("Buck Trends3", 20, a1)
```

- secondary constructor(보조 생성자)를 사용하는 경우는 많이 없고, apply factory method를 많이 활용하자.

## 8.6 클래스의 필드

**Uniform Access Principle(단일 접근 원칙)**

field 값을 그대로 사용하던 계산된 값을 사용하던 Client 쪽에서는 같은 방식으로 접근하도록 해야하는 것. 구현과 종속 X.

> The essential point of the principle is that if you have a person object and you ask it for its age, you should use the same notation whether the age is a stored field of the object or a computed value. It effectively means that a client of the person should neither know nor care whether the age is stored or computed.
> 

ref. [https://martinfowler.com/bliki/UniformAccessPrinciple.html](https://martinfowler.com/bliki/UniformAccessPrinciple.html)

```scala
class Name(val value: String)
//val name = new Name("Buck")
//name.value

class Name2(val s: String) {
	var value: String = s
}
//val name = new Name("Buck")
//name.value
```

→ Scala는 위와 같은 식으로 필드와 method 접근을 동일한 방식으로 제공할 수 있어서, 단일 접근 원칙을 따른다(자바는 이 경우에 지원이 어렵)

→ OOP 측면에서 encapsulation의 중요 요소이다.

## 8.8 좋은 객체지향 설계

**상속보다 합성**: Trait를 사용하면 합성을 훨씬 쉽게 할 수 있다.

ref. [https://github.com/coukieStudy/Effective-Java/blob/master/item%2018/origin.md](https://github.com/coukieStudy/Effective-Java/blob/master/item%2018/origin.md)

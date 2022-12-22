# 15. 스칼라 타입 시스템 2

## 15.1 경로에 의존하는 타입

```scala
// src/main/scala/progscala2/typesystem/typepaths/type-path.scalaX
package progscala2.typesystem.typepaths

class Service {                                                      // <1>
  class Logger {
    def log(message: String): Unit = println(s"log: $message")       // <2>
  }
  val logger: Logger = new Logger
}

val s1 = new Service
val s2 = new Service { override val logger = s1.logger }     // ERROR!  <3>

// error: overriding value logger in class Service of type this.Logger;
// value logger hase incompatible type
```

스칼라는 각 Service 인스턴스의 logger를 서로 다른 타입으로 인식한다. 즉 경로에 의존한다.

### 15.1.1 C.this

```scala
class C1 {
  var x = "1"
  def setX1(x:String): Unit = this.x = x
  def setX2(x:String): Unit = C1.this.x = x // this는 C1.this
}

trait T1 {
  class C
  val c1: C = new C
  val c2: C = new this.C // 여기서 this는 T1 trait
}
```

### 15.1.2 C.super

```scala
trait X {
  var xx = "xx"
  def setXX(x:String): Unit = xx = x
}
class C2 extends C1
class C3 extends C2 with X {
  def setX3(x:String): Unit = super.setX1(x) // super는 C3.super
  def setX4(x:String): Unit = C3.super.setX1(x) // 부모 명시안하면 선형화 규칙에따라서 X가 super.
  def setX5(x:String): Unit = C3.super[C2].setX1(x)
  def setX6(x:String): Unit = C3.super[X].setXX(x)
  // def setX7(x:String): Unit = C3.super[C1].setX1(x)    // ERROR 조부모 타입 참조못함
  // def setX8(x:String): Unit = C3.super.super.setX1(x)  // ERROR super 연쇄 사용 불가
}
```

### 15.1.3 경로.x

- 내포된 타입에 접근하려면 마침표를 사용한 경로식을 사용하면 된다. 어떤 타입 경로의 맨 마지막 부분을 제외한 나머지 부분은 안정적이어야 한다.
- 대략적으로 이는 패키지, 싱글턴 객체, 또는 그 둘에 대한 타입 별명이어햐 한다.
- 경로의 마지막은 안정적일 필요 없다. 그래서 클래스, 트레이트, 타입 멤버 등을 사용할 수 있다.
- 복잡한 경로 사용 권장 안함.

```scala
package P1 {
  object O1 {
    object O2 {
      val name = "name"
    }
    class C1 {
      val name = "name"
    }
  }
}
class C7 {
  val  name1 = P1.O1.O2.name      // Okay  - a reference to a field
  type C1    = P1.O1.C1           // Okay  - a reference to a "leaf" class
  val  c1    = new P1.O1.C1       // Okay  - same reason
  // val name2 = P1.O1.C1.name    // ERROR - P1.O1.C1 isn't stable.
}
```



## 15.2 의존적 메서드 타입

```scala
case class LocalResponse(statusCode: Int)
case class RemoteResponse(message: String)

sealed trait Computation {
  type Response
  val work: Future[Response]
}

case class LocalComputation(
    work: Future[LocalResponse]) extends Computation {
  type Response = LocalResponse
}
case class RemoteComputation(
    work: Future[RemoteResponse]) extends Computation {
  type Response = RemoteResponse
}

object Service {
  def handle(computation: Computation): computation.Response = {
    val duration = Duration(2, SECONDS)
    Await.result(computation.work, duration)
  }
}

Service.handle(LocalComputation(Future(LocalResponse(0))))
// Result: LocalResponse = LocalResponse(0)
Service.handle(RemoteComputation(Future(RemoteResponse("remote call"))))
// Result: RemoteResponse = RemoteResponse(remote call)
```

- handle이 공통의 슈퍼클래스의 인스턴스를 반환하지 않는다! 
- LocalResponse, RemoteResponse가 전형 관계 없다. 대신 handle은 인자에 의존하는 타입을 반환한다.



## 15.3 타입 투영

https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/typesystem/valuetypes/type-projection.scala

```scala
import progscala2.typesystem.valuetypes._

val l1: Service.Log   = new ConsoleLogger    // ERROR: No Service "value"
val l2: Service1.Log  = new ConsoleLogger    // ERROR: No Service1 "value"
val l3: Service#Log   = new ConsoleLogger    // ERROR: Type mismatch
val l4: Service1#Log  = new ConsoleLogger    // Works!
```

- l1, l2 는 Service나 Service1이라는 객체를 찾아야한다는 것을 의미하지만 없음..
- 하지만 타입 이름을 #을 사용해서 투영할 수 있다.
- l3은 Logger의 서브타입이지만 ConsoleLogger와 호환되지 않을 수 있기때문에 error.

### 15.3.1 싱글턴 타입

싱글턴 객체는 인스턴스를 하나 정의하는 동시에 그에 대응하는 타입도 하나 정의한다.

```scala
case object Foo { override def toString = "Foo says Hello!" }

def printFoo(foo: Foo.type) = println(foo) //Foo.type이 Foo에 대응하는 타입!
```



## 15.4 값에 대한 타입

### 15.4.1 튜플 타입

```scala
val t1: Tuple3[String, Int, Double] = ("one", 2, 3.14)
val t2: (String, Int, Double) = ("one", 2, 3.14)
```

### 15.4.2 함수 타입

```scala
val f1: Function2[Int, Double, String] = (i, d) => s"int $i, double $d"
val f2: (Int, Double) => String = (i, d) => s"int $i, double $d"
```

### 15.4.3  중위 타입

```scala
val left1: Either[String, Int] = Left("hello")
val left2: String Either Int = Left("hello")
val right1: Either[String, Int] = Right(1)
val right2: String Either Int = Right(1)
```



## 15.5 고계 타입

```scala
def sum(seq: Seq[Int]): Int = seq reduce (_+_)
sum(Vector(1,2,3,4,5)) // Result: 15
```



## 15.6 람다 타입

https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/typesystem/typelambdas/Functor.scala

```scala
List(1,2,3) map2 (_ * 2)               // List(2, 4, 6)
Option(2) map2 (_ * 2)                 // Some(4)
val m = Map("one" -> 1, "two" -> 2, "three" -> 3)
m map2 (_ * 2)                         // Map(one -> 2, two -> 4, three -> 6)
```

- 타입 람다는 Functor가 지원하지 못하는 Map에 대한 추가 타입 매개변수를 처리해준다.
- α는 코드에서 추론된다.



## 15.7 자기 재귀 타입

자기자신을 참조하는 타입

```scala
public abstract class Enum<E extends Enum<E>>
extends Object
implements Comparable<E>, Serializable
```

같은 타입에 정의된 열거값 중 하나가 아닌 객체를 compareTo에 넘기는 것은 컴파일 오류다.

```scala
TimeUnit.MILLISECONDS compareTo Type.HTTP // error!
```


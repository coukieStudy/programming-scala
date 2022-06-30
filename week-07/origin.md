# 5. 암시(implicit)

## 5.1 암시적 인자
https://docs.scala-lang.org/tour/implicit-parameters.html
암시적 파라미터에 주는 인자를 생략할 경우, 생략된 인수는 자동적으로 제공될 것이다.

### 5.1.1 implicitly 사용하기
[implicitly](https://dotty.epfl.ch/api/scala/Predef$.html#implicitly-957)는 Predef에 이미 메서드로 정의되어있다. 아래 sortBy1, sortBy11, sortBy2 모두 같은 동작을 한다.

```scala
case class MyList[A](list: List[A]) {
  def sortBy1[B](f: A => B)(implicit ord: Ordering[B]): List[A] =
    list.sortBy(f)(ord)

  def sortBy11[B](f: A => B)(implicit ord: Ordering[B]): List[A] =
    list.sortBy(f)(implicitly(Ordering[B]))

  def sortBy2[B : Ordering](f: A => B): List[A] =
    list.sortBy(f)(implicitly[Ordering[B]])
}

val list = MyList(List(1,3,5,2,4))

list sortBy1 (i => -i)  // res0: List[Int] = List(5, 4, 3, 2, 1)
list sortBy11 (i => -i) // res1: List[Int] = List(5, 4, 3, 2, 1)
list sortBy2 (i => -i)  // res2: List[Int] = List(5, 4, 3, 2, 1)
```

## 5.2 암시적 인자를 사용하는 시나리오
암시는 과도하게 사용하면 코드 읽기 어려울 수 있다. 하지만 현명하게 사용하면 1) 준비를 위한 코드를 없애는 것이 가능하고 2) 매개변수화한 타입을 받는 메서드에 사용해서 버그를 줄이거나 허용되는 타입을 제한할 수 있다.

### 5.2.1 실행 맥락 제공하기 
```scala
// https://github.com/deanwampler/programming-scala-book-code-examples/blob/master/src/main/scala-2/progscala3/concurrency/async/Async.scala
import scala.concurrent.ExecutionContext.Implicits.global

object AsyncExample {
  def asyncGetRecord(id: Long): Future[(Long, String)] = async {
    val exists = async { val b = recordExists(id); println(b); b }
    if (await(exists)) await(async { val r = getRecord(id); println(r); r })
    else (id, "Record not found!")
  }
}
```
ex) [Async.scala](https://github.com/scala/scala-async/blob/0f56a07cfffd8b097777e7ce8e25221593545c7b/src/main/scala/scala/async/Async.scala#L54)

### 5.2.2 사용 가능한 기능 제어하기
```scala
def createMenu(implicit session: Session): Menu = {
  val defaultItems = List(helpItem, searchItem)
  val accountItems =
    if (session.loggedin()) List(viewAccountItem, editAccountItem)
    else List(loginItem)
  Menu(defaultItems ++ accountItems)
}
```

### 5.2.3 사용 가능한 인스턴스 제한하기
암시적 인자를 넘기고 우리가 원하는 타입하고만 일치시킬 수 있는 암시적 값을 정의하는 방식을 통해 매개변수화한 메서드에 사용할 수 있는 타입을 제한한다.
```scala
// src/main/scala/progscala2/implicits/scala-database-api.scala
// A Scala wrapper for the Java-like Database API.
package progscala2.implicits {
    package scaladb {
    object implicits {
      import javadb.JRow

      implicit class SRow(jrow: JRow) {
        def get[T](colName: String)(implicit toT: (JRow,String) => T): T =
          toT(jrow, colName)
      }

      implicit val jrowToInt: (JRow,String) => Int =
        (jrow: JRow, colName: String) => jrow.getInt(colName)
      implicit val jrowToDouble: (JRow,String) => Double =
        (jrow: JRow, colName: String) => jrow.getDouble(colName)
      implicit val jrowToString: (JRow,String) => String =
        (jrow: JRow, colName: String) => jrow.getText(colName)
    }

    object DB {
      import implicits._

      def main(args: Array[String]): Unit = {
        val row = javadb.JRow("one" -> 1, "two" -> 2.2, "three" -> "THREE!")

        val oneValue1: Int      = row.get("one")
        val twoValue1: Double   = row.get("two")
        val threeValue1: String = row.get("three")
        // val fourValue1: Byte    = row.get("four")  // won't compile

        println(s"one1   -> $oneValue1")
        println(s"two1   -> $twoValue1")
        println(s"three1 -> $threeValue1")

        val oneValue2   = row.get[Int]("one")
        val twoValue2   = row.get[Double]("two")
        val threeValue2 = row.get[String]("three")
        // val fourValue2    = row.get[Byte]("four")  // won't compile

        println(s"one2   -> $oneValue2")
        println(s"two2   -> $twoValue2")
        println(s"three2 -> $threeValue2")
      }
    }
  }
}

```

### 5.2.4 암시적 증거 제공하기
ex) 모든 순회가능 컬렉션에 있는 toMap 메서드
Map 생성자가 키-값 쌍을 인자로 요구한다. 시퀀스가 쌍의 시퀀스가 아닌 경우에 toMap을 호출하지 못하도록함.
 ```scala
 trait TraversableOnce[+A] ... {
   ...
   def toMap[T, U](implicit ev: <:<[A, (T, U)]): immutable.Map[T, U] // A <:< (T, U)
   ...
 }
 ```
 ```bash
 scala> val l1 = List(1,2,3)
l1: List[Int] = List(1, 2, 3)

scala> l1.toMap
<console>:12: error: Cannot prove that Int <:< (T, U).
       l1.toMap
          ^

scala> val l2 = List("one" -> 1, "two" -> 2, "three" ->3)
l2: List[(String, Int)] = List((one,1), (two,2), (three,3))

scala> l2.toMap
res1: scala.collection.immutable.Map[String,Int] = Map(one -> 1, two -> 2, three -> 3)

 ```

 ### 5.2.5 타입 소거 우회하기
 JVM은 매개변수화한 타입의 타입 인자를 망각한다. 이때문에 생기는 오버로딩시 컴파일에러 피하기 위해서.
 implicit-erasure.sc

 ### 5.2.6 오류메세지 개선하기
```scala
package scala.collection.generic
@scala.annotation.implicitNotFound("Cannot construct a collection of type ${To} with elements of type ${Elem} based on a collection of type ${From}.")
trait CanBuildFrom[-From, -Elem, +To] extends scala.AnyRef {
  def apply(from : From) : scala.collection.mutable.Builder[Elem, To]
  def apply() : scala.collection.mutable.Builder[Elem, To]
}
```

### 5.2.7 |> 연산자
phantom-types-pipeline.scala
```scala
Payroll.minus401k(pay1)

val pay1 |> minus401k
```

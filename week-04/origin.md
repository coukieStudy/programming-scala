## 3.7 다른 루프 표현

### While 문

```scala
def isFridayThirteen(cal: Calendar): Boolean = {
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

    (dayOfWeek == Calendar.FRIDAY) && (dayOfMonth == 13)
}

while (!isFridayThirteen(Calendar.getInstance())) {
    println("오늘은 13일금요일이 아닙니다")            
}
```

Scala에는 break라는 예약어가 없습니다.

- [How do I break out of a loop in Scala? - Stack Overflow](https://stackoverflow.com/a/5138410)

### do-while 루프

```scala
var count = 0

do {
    count += 1
    println(count)
} while (count < 10)
```

사용하는 케이스가 굉장히 드뭅니다.

따라서 Scala3에서는 없어진 문법입니다

- [Dropped: Do-While](https://dotty.epfl.ch/docs/reference/dropped-features/do-while.html)

## 3.8 조건 연산자

- 대부분 자바와 유사합니다
  
- &&, || 는 쇼트서킷을 지원합니다
  

### java와 scala에서 ==의 차이점

- 스칼라에서는 논리적 동등성을 비교할 때 ==를 사용합니다
  
- 자바에서와 같이 객체 동등성을 비교하고 싶을 때는 `eq` 라는 새로운 메서드를 사용합니다
  
- 단 equals를 따로 오버라이딩하지 않는 경우, ==는 동일한 객체만 같다고 계산합니다
  
- Scala에서 == 의 비교가 null일때도 작동합니다
  
  - [Scala Standard Library 2.13.8 - scala.Any](https://www.scala-lang.org/api/2.13.8/scala/Any.html#==(x$1:Any):Boolean)
    
  - java의 경우 null.equals(something)이 안되지만 scala에서는 null == something이 가능합니다
    
  - java는 그래서 보통 String 비교시에도 `someStr.equals("test")` 가 아닌 `"test".equals(someStr)` 을 사용합니다
    

## 3.9 try, catch, finally 사용하기

- 스칼라에서는 Checked Exception이 존재하지 않습니다.

```scala
object TryCatch {
    def main(args: Array[String]) = {
        argsforeach (arg=>countLines(arg))
    }
    import Scala.io.Source
    import SCala.util.control.NonFatal
    def CountLines(fileName: String) = { 
        println()
        var source: Option[Source] = None
        try {
            source = Some(Source.fromFile(fileName))
            val size = source.get.getLines.Size
            println(s"file $fileName has $Size lines")
        } catch { 
            case NonFatal(ex) => println(s"Non fatal exception! $ex")
        } finally {
            for (S <- source) {
                println(s"Closing $fileName...")
                s.close 
            }
        }
    }
}
```

- pattern match를 사용해서 exception을 처리합니다.

### try match 문

```scala
      val latestVal = Try(FileUtils.getLatstPartition(spark, latestPath, s"$key=")) match {
        case Success(result) => result
        case Failure(_) => null
      }
```

조금 더 함수형답게 에러처리를 하기 위해서는 Try - match 문을 사용할 수 있습니다

- https://docs.scala-lang.org/overviews/scala-book/functional-error-handling.html#trysuccessfailure

## 3.10 Call by name, Call by value

Call by Name은 사용될 때 마다 실행됩니다.

문법은 `=>` 를 사용합니다

```scala
def calculate(input: => Int) = input * 37
```

이를 이용해서 DSL 과 같은 구조를 만들 수 있습니다

```scala
def whileLoop(condition: => Boolean)(body: => Unit): Unit =
  if (condition) {
    body
    whileLoop(condition)(body)
  }

var i = 2

whileLoop (i > 0) {
  println(i)
  i -= 1
}  // prints 2 1
```

(책 내용은 이해하기 힘든 부분이 많아서 scala doc을 참고 했습니다. https://docs.scala-lang.org/tour/by-name-parameters.html)

whileLoop는 1. 커링 2. 익명함수 3. 소괄호 생략 을 통해 새로운 문법 처럼 보이도록 작동합니다([Curly braces in Scala method call - Stack Overflow](https://stackoverflow.com/questions/49706714/curly-braces-in-scala-method-call))

```scala
whileLoop(i > 0)({println(i);i -= 1})
```

## 3.11 지연값

데이터베이스 연결과 같이 계산 비용이 비싼 경우에 실제 사용될 때까지 식을 계산하지 않습니다.
Call by Name은 실행 될 때 마다 계산되는 것과 다르게 지연값은 최초 계산될 때만 실행됩니다.

```scala
object ExpensiveResource {
    lazy val resource: Int = init()
    def init(): Int = {
        // 오래 걸리는 작업
        0
    }
}
```

- lazy는 var에 사용할 수 없습니다.

## 3.12 열거값

스칼라에서는 표준 라이브러리의 Enumeration을 통해 열거값을 구현하기 때문에 자바의 enum 사이와는 아무런 연관이 없습니다.

```scala
object Fingers extends Enumeration {
  type Finger = Value

  val Thumb, Index, Middle, Ring, Little = Value
}
```

Enumeration의 Value는 자동으로 id 값이 지정됩니다.

```scala
@Test
def givenAFinger_whenIdAndtoStringCalled_thenCorrectValueReturned() = {
  assertEquals(0, Thumb.id)
  assertEquals("Little", Little.toString())
}
```

String 값을 같이 사용하려면 다음과 같이 정의할 수 있습니다.

```scala
val Thumb = Value(1, "Thumb Finger")
val Index = Value(2, "Pointing Finger")
val Middle = Value(3, "The Middle Finger")
val Ring = Value(4, "Finger With The Ring")
val Little = Value(5, "Shorty Finger")
```

Ref : [Guide to Scala Enumerations | Baeldung on Scala](https://www.baeldung.com/scala/enumerations)

스칼라에서는 값의 열거가 필요할 때 열거 값 대신에 케이스 클래스를 사용합니다

- 클라이언트에서 다른 케이스 클래스 추가 가능
- 패턴 매칭 사용 가능

## 3.13 문자열 인터폴레이션

### 기본적인 문자열 인터폴레이션 (s 인터폴레이터)

```scala
val name = "Buck Trends"
println(s"Hello, $name")
println(s"1 + 1 = ${1 + 1}") // 표현식도 가능
```

### 포맷팅 문자열 인터폴레이션 (f 인터폴레이터)

```scala
val height = 1.9d
val name = "James"
println(f"$name%s is $height%2.2f meters tall")  // James is 1.90 meters tall
```

### 로우 인터폴레이션 (raw 인터폴레이션)

이스케이프 문자를 변환하지 않습니다.

```scala
scala> s"a\nb"
res0: String =
a
b


scala> raw"a\nb"
res1: String = a\nb
```

## 3.14 트레이트: 스칼라 인터페이스와 혼합

스칼라에는 인터페이스 대시 트레이트가 존재합니다.

"메서드를 선언하면서 원하면 정의까지 할 수 있는 인터페이스" -> 자세한 내용은 9장에서 확인 가능합니다. (사실 현재 자바에서 default가 인터페이스에 생기면서 이 부분은 이상한 표현이 되었습니다.)

트레이트는 원한다면 인스턴스 필드를 선언하고 정의할 수 있습니다.

```scala
trait Composition {
  var composer: String

  def compose(): String
}

class Score(var composer: String) extends Composition {
  override def compose(): String = s"The score is composed by $composer"
}
```

트레이트의 핵심은 믹스인(mixin)을 사용할 수 있다는 점입니다.

```scala
class ServiceImportante(name: String) {
    def work(i: Int): Int = {
        println(s"ServiceImportante: Doing important work! $i")
        i+1
    }
}

trait Logging {
    def info(message: String): Unit
    def warning(message: String): Unit
    def error(message:String): Unit
}
trait StdoutLogging extends Logging {
    def info(message: String) = println(s"INFO: $message")
    def warning(message: String) = println(s"WARNING: $message")
    def error(message: String) = println(s"ERROR: $message")
}

// with으로 mixin 할 수 있다
// instance에도 mixin 할 수 있
val service = new ServiceImportante("dos") with StdoutLogging {
    override def work(i: Int): Int = {
        info(s"Starting work: i = $i")
        val result = super.work(i)
        info(s"Ending work: i = $i, result = $result")
        result
    }
}

(1 to 3) foreach (i => println(s"Result: ${service.work(i)}"))
```

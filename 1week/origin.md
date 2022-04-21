# 2장 입력은 조금만, 일은 더 많이

## SBT 설치

[https://www.scala-sbt.org/download.html](https://www.scala-sbt.org/download.html)

[https://github.com/deanwampler/programming-scala-book-code-examples](https://github.com/deanwampler/programming-scala-book-code-examples)

```scala
class Upper {
	def upper(strings: String*): Seq[String] = { //가변길이 매개변수 목록
		strings.map((s: String) => s.toUpperCase())
	}
} 

val up = new Upper
println(up.upper("Hello","World!"))
```

```scala
object Upper {
	def upper(strings: String*) = strings.map(_.toUpperCase())
}

println(Upper.upper("Hello","World!"))
```

- object는 싱글턴 객체. 따라서, new Upper 사용 불가능
- Scala 타입 추론 알고리즘: 지역 타입 추론(?)  - 일정 영역 내에서 작동한다는 뜻
    - 인자가 지켜야할 타입을 추론할 수는 없지만, 함수 본문은 볼 수 있기에 메세드가 반환하는 타입은 추론할 수 있다.
- 등호를 사용하기 때문에 매개변수를 받지 않는 경우, {} 생략 가능
- _(placeholder): 인자가 하나라면 placeholder로 사용 가능
- **저자: 반환 타입을 명시하는 것을 더 권장 (명확성)**

## 문법

- 세미콜론 추론: [semicolon-example.sc](http://semicolon-example.sc)
- 변수 정의: person.sc
    - val: immutable
    - var: mutable - 나중에 변경은 가능하지만, 선언할 때 반드시 초기화
    - Java와 같이 값을 표현하는 primitive type들이 존재 X(char, byte, short..)
    - 변경으로 인해 생길 수 있는 버그 유형을 방지하기 위해 가능한 한 변경 불가능한 값을 활용하라.
- 범위(Range): ranges.sc
- 부분함수(PartialFunction): partial-function.sc
    - 지정한 케이스 절에서 어느 하나와 일치하는 입력에 대해서만 결과를 정의한다.
- 메서드 선언: progscala2/typelessdomore/shapes/shapes.scala
- Future: 스칼라가 제공하는 동시성 도구 - futures.sc
    - 인자 목록을 명시하지 않는다면, 기본 ExecutionContext를 사용. 암시적으로 선언해두었기에 가능
        - implicit로 선언된 것들은 명시하지 않고 호출하는 경우, compiler가 implicit로 선언되어있는 것을 사용한다. `[ExecutionContext.Implicits.*global](http://ExecutionContext.Implicits.global)`*
    - ExecutionContext: ForkJoinPool 기능을 사용해서 자바 스레드를 관리한다.

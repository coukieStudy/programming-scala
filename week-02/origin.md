# 입력은 조금만, 일은 더 많이
## 2.5.4 내포된 메서드 정의와 재귀
* factorial.sc
* factorial-tailrec.sc
* count-to.sc

## 2.6 타입 정보 추론하기
```scala
//java
HashMap<Integer, String> intToStringMap = new HashMap<>();
//scala
val intToStringMap: HashMap[Integer, String] = new HashMap
val intToStringMap2 = new HashMap[Integer, String]
```
### 명시적 타입 표기가 필요한 경우
* var, val 선언에서 값을 대입하지 않은 경우 (val book:String)
* 모든 메서드 매개변수 (def doposit(amount: Money) = {...})
* 다음과 같은 메서드 반환 타입의 경우
  * 메서드 안에서 return을 명시적으로 호출하는 경우
  * 메서드 재귀적인 경우
  * 오버로딩한 둘 이상의 메서드가 있고, 그중 한 메서드가 다른 메서드를 호출하는 경우, 호출하는 메서드 (method-overloaded-return-v1.scX)
  * 컴파일러가 추론한 타입이 우리의 의도보다 더 일반적인 경우(method-broad-inference-return.scX) -> rarely

### 흔한 타입 오류
```scala
scala> def double(i: Int) { 2 * i }
double: (i: Int)Unit

scala> println(double(2))
()

scala> def double(i: Int) = { 2 * i }
double: (i: Int)Int

scala> println(double(2))
4
```

## 2.7 예약어
https://github.com/FlyScala/ProgrammingScala/wiki/%EC%98%88%EC%95%BD%EC%96%B4 
- break, continue가 없음
- java.util.Scanner.match와 같은 일부 자바 메서드의 이름이 스칼라에서 예약어다.
- 이 경우에는 java.util.Scanner.`match`와 같이 백틱(back tick)을 써야한다.


## 2.8 리터럴 값

### 2.8.1 정수 리터럴
- 10진수, 16진수, 8진수
- Long, Int, Short, Char, Byte
- Long은 L 또는 l 필요
- 리터럴의 범위를 넘어서면 컴파일 시 오류 발생

### 2.8.2 부동소수 리터럴
- Float(32bit 단정도 2진 부동소수점)
- Double(64bit 배정도 2진 부동소수점)
- 스칼라 2.10 이전에는 소수점 뒤에 숫자가 없는 부동소수점 리터럴(3., 3.e5)이 있었으나 애매한 경우가 있어서 2.11부터 허용 안됨

### 2.8.3 불린 리터럴
true, false

### 2.8.4 문자 리터럴
인쇄 가능한 유니코드 문자나 이스케이프 시퀀스

### 2.8.5 문자열 리터럴
- "" : 기본 문자열
- """...""" : 멀티라인 문자열 (이스케이프 시퀀스를 해석 하지 않는다.)
- stripMargin(| 대신 사용하고 싶은 Char) : 형식화를 위해 위치를 맞춘 경우 공백 컨트롤, 문자 앞에 공백 제거
- stripPrefix(접두사)
- stripSuffix(접미사)

### 2.8.6 심벌 리터럴
- 동일 심벌은 하나의 객체를 참조한다.
- 'id == scala.Symbol("id")
- 공백을 포함하는 심벌 생성 방법 : Symbol(" Programming scala ")

### 2.8.7 함수 리터럴
```scala
scala> (i: Int, s: String) => s+i   // Function2[Int,String,String]타입의 함수 리터럴
res: (Int, String) => String
scala> val f1: (Int, String) => String = (i, s) => s+i
f1: (Int, String) => String
```

### 2.8.8 튜플 리터럴
- N개의 원소를 묶어주는 TupleN 클래스
- TupleN은 현재 1~22 까지 가능하나 한계는 추후에 없어질 수 있다.
- tuple-example.sc

## 2.9 Option, Some, None: null 사용 피하기
- Option 추상클래스
- 서브클래스 Some, None
- state-capitals-subset.sc

## 2.10 봉인된 클래스 계층
- Option abstrac class/ subclass : Some, None
- sealed
- 모든 서브 클래스가 같은 소스 파일 안에 선언되어야 한다.
- 사용자가 서브타입을 만들수없다.
```
sealed abstract class Option[+A] extends Product with Serializable {...}
```

## 2.11 파일과 이름공간으로 코드 구조화하기
- 파일이름은 타입이름과 달라도 된다.
- 패키지 구조는 디렉토리 구조와 달라도 된다.
- 패키지 영역을 선언하기 위해 블록 구문을 사용하도록 지원한다. (package-example2.scala)
- package-example3.scala
- 단 한가지 제약 사항은 클래스 또는 객체 안에서 패키지를 정의 할 수 없다.

## 2.12 타입과 멤버 임포트하기

```scala
import java.awt._	// 밑줄(_): 패키지 안에 모든 타입 임포트
import java.io.File 
import java.io.File._ // 모든 정적 메서드와 필드 => java의 import static java.io.File.*
import java.util.{Map, HashMap}
```

```scala
def stuffWithBigInteger() = {
	import java.math.BigInteger.{
	ONE => _,
	TEN,
	ZERO => JAVAZERO
	}
	// println("ONE: "+ONE) // ONE은 정의되지 않은 상태
	println("TEN: "+TEN)
	println("ZERO: "+JAVAZERO)
}
```

- 임포트 문은 아무데나 위치 가능
- 임포트 하면서 별명 부여 가능
- 임포트는 상대적이다. (relative-imports.scala)

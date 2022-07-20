# Chapter 6 스칼라 함수형 프로그래밍
> 한 종류의 데이터 구조에 쓸 수 있는 함수가 100개 있는 것보다, 10종류의 데이터 구조에 쓸 수 있는 함수가 10개 있는게 더 낫다.

- 객체에 대한 재활용이 아니라, 핵심 자료구조와 함수를 재활용한다
- 동시성 프로그램에 대한 수요 -> 함수형 프로그래밍의 부상
	- 데이터 중심 프로그래밍
	- 순수함수
	- 불변성

## 6.1 함수형 프로그래밍이란 무엇인가?
- 참조 투명성의 의의
	- 1. 함수를 아무데서나 독립적으로 호출하여 동일한 동작을 확신
	- 2. 어떤 식이 2번 이상 반복될 때 식의 첫번 째 결과를 이용해 식을 치환할 수 있다
		- 따라서 함수가 1급 계층값
			- 함수를 인자로 넘길 수도 있고, 반환할수도 있다
			- 이런 함수들을 **고차함수** 라고 한다
- 함수형 프로그래밍은 상태가 없다?
	- 순수하지 않은 함수는 프로그래밍의 목적
	- 함수형 프로그래밍은 상태를 항상 새로운 인스턴스나 새로운 스택프레임으로 표현할 뿐이다
	- `factorial.sc` 파일을 보면 스택프레임으로 중간 상태가 저장된다
- 불변성
	- 변경 불가능한 데이터 구조(val)을 사용
	- 데이터 오염에 대한 걱정이 없어 getter, setter 없이 내부 데이터를 공개 가능
	- 물론 API를 위해 데이터 노출을 줄이고 추상화 하는 것은 함수형에서도 중요하다

## 6.2 스칼라 함수형 프로그래밍
- 고차 함수의 예시
	- ```scala
		(1 to 10) filter (_ % 2 == 0) map (_ * 2) reduce (_ * _)
	- `_ % 2 == 0` 등은 **함수 리터럴** 이다
- 클로저
	- closure-example.sc 를 보면 val 로 고정된 함수임에도 불구하고 factor가 바뀜에 따라 함수의 동작이 바뀐다
	- i = 형식인자 (formal parameter), factor = 자유변수 (free variable)
	- 컴파일러가 multiplier 안의 문맥과 자유 변수가 참조하는 외부 문맥을 포함하여 함께 '닫혀있는' 클로져를 만든다
	- % 개인적으로는 함수에 상태를 추가한 것처럼 느껴졌다
- 안에서 보는 순수성과 밖에서 보는 순수성
	- 외부에서 봤을 때는 순수함수더라도, 내부에서 성능 향상을 위해 메모이제이션과 같이 캐싱을 할 수 있다
	- 단 이는 내부의 구현 문제이지, 외부에서 봤을 때는 스레드 안정성과 참조 투명성이 보존되어야 한다

## 6.3 재귀 & 6.4 꼬리 호출과 꼬리 호출 최적화
재귀는 함수형 언어에서 루프 변수 대신, 스택 프레임으로 상태를 저장하여 루프를 구현하는 방법

### 꼬리 호출
- 조건
	- 1. 함수가 자기 자신을 호출
	- 2. 그 호출이 해당 함수의 가장 마지막 연산
- 꼬리 호출은 컴파일러가 루프로 가장 쉽게 변환할 수 있는 재귀 호출 유형이기 때문에 최적화가 쉽다
- `factorial-recur2.sc`
- 누적시키기 위한 값을 추가하는 방법으로 재귀를 꼬리 재귀로 많이 바꾼다
- 꼬리 재귀는 private, final 혹은 다른 메서드 내부에 존재해야 한다
- 꼬리 호출 트램펄린
	- A -> B -> A -> B 와 같이 서로 다른 함수가 호출을 주고 받는 구조
	- `TailCalls`라는 지정된 객체를 사용해야 한다

## 6.5 부분 적용 함수와 부분 함수
### 부분 적용 함수
- `curried-func.sc`
- 파라미터 중 일부만 적용해서 새로운 함수를 정의하는 것
- 부분 적용 기능은
	- 한 인자 목록 안에 여러 인자 중 생략 X e.g. (i: Int, s: String)
	- 여러 인자 목록 안에서 뒤에 목록 생략 O e.g. (i: Int)(s:String)
### 부분 함수
- 2.4절
- 함수가 인자를 하나만 받아 그 타입이 취할 수 있는 모든 값중 일부에만 정의되있는 함수

## 6.6 함수의 커링과 다른 변환
- 커링 변환
	- 여러 인자를 취하는 함수 -> 단 하나의 인자를 취하는 여러 함수의 연쇄
- How
	- `.curried` 함수를 이용해 여러 인자를 취하는 메서드를 커링 형태로 바꿀 수 있다
- 커링과 부분 적용함수는 서로 밀접한 연관이 있어, 이 두 개념이 거의 구분없이 쓰인다
- 커링의 활용
	- 함수를 일반적인 경우로 시작해서, 커링을 통해 특별한 경우로 좁혀나간다
- 다른 변환들
	- `Function.tupled`
	- `Function.untupled`
	- `Function.lift`
	- `Function.unlift`

## 6.7 함수형 데이터 구조
> 표현하고자 하는 개념에 맞는 임의의 클래스를 만드는 것이 훨씬 일반적인 객체지향 언어에 비해 함수형 프로그래밍에서는 핵심 데이터 구조와 알고리즘을 사용하는 것을 매우 강조한다. 코드 재사용이 객체지향 프로그래밍의 약속이긴 하지만, 이런 식의 임의 클래스가 너무 많으면 그런 목표를 달성하기 어렵다

- 함수형 프로그래밍에서는 핵심 데이터 구조와 알고리즘을 제공하고, 이를 합성(combinator라는 함수들로) 해서 사용한다

### 시퀀스
- 순차적인 데이터
- `collection.Seq`, `collection.mutable.Seq`, `collection.immutable.Seq`
- Seq는 trait 이다
- 원소에 리스트를 추가하면 **기존 리스트의 맨 앞에 들어가서** 새로운 리스트의 '머리'가 된다

#### List
- 새 리스트를 기존 리스트로부터 O(1) 연산으로 만들 수 있다
	- 복사에 드는 비용을 최소화하기 위해 구조를 공유
	- ![[Pasted image 20220720135412.png]]
- List는 Seq의 구현체이다. 따라서 타입 정의시 List 대신 Seq을 사용하는 것을 권장
- Seq의 경우 `::`가 아닌 `+:` 를 사용한다
- 뒤에 붙일 때는 `:+`를 사용한다
- Seq는 트레이트기 때문에 Seq를 이용해서 생성시 동반 객체를 이용해 List를 만든다

#### Vector
- `immutable.Vector`
- 모든 연산에서 O(1)연산을 제공
- Tree 구조를 구현에 사용
- List는 head 접근 연산을 제외하고 모든 연산이 O(N)
- 언제 Vector를 쓸 수 있을까
	- https://stackoverflow.com/questions/6928327/when-should-i-choose-vector-in-scala
- 콜렉션에 따른 연산 시간 비교
	- https://docs.scala-lang.org/overviews/collections-2.13/performance-characteristics.html
	- https://www.baeldung.com/scala/vector-benefits
#### 시퀀스의 주의사항
- 기본 Seq는 `collection.Seq`로 `collection.immutable.Seq` 가 아니다.
- 따라서 Seq를 함수 인자로 한다면 mutable한 Collection이 올 수 있다
- scala.collectin.immutable.Seq를 사용하도록 강제할 것을 권한다

### 맵
`map.sc`
### 집합
`set.sc`

## 6.8 순회하기, 연관시키기, 걸러내기, 접기, 축약하기
> 이제부터는 각 메서드의 동직◇II만 집중하기 위해 단순화한 시그니처만 보여줄 것이다. 히지만
List (http://bit.ly/1toub3N)나 Map(http://bit.ly/15y3BGn)과 같은 컬렉션을 하나 선택해서 각 메서드의 전체 시그니처를 볼 것을 권징한다. 이런 시그니처를 읽는 법을 배우는 것은 처음에는 약간 벅찰  것이다. 하지만 이는 컬렉션을 효율적으로 사용하기 위해서는 필수적이다.

### 순회
- foreach
	-  scala.collection.IterableLike
	- https://www.scala-lang.org/api/2.12.3/scala/collection/IterableLike.html#foreach(f:A=%3EUnit):Unit
	- 대부분의 Bulk Operation이 이를 통해 구현되기 때문에 Subclass에서 더 효율적인 구현이 있으면 overriding 할 것을 언급하고 있다
### 연관시키기
- map
	- [TraversableLike](https://www.scala-lang.org/api/2.12.3/scala/collection/TraversableLike.html)에 존재
	-  map을 컬렉션에 대해 함수 f를 적용해서 새로운 컬렉션을 만드는 함수가 아니라, **평범한 함수 f:A => B를 새로운 함수 flist:List[A] => LIst[B]로  끌어올리는 도구로 사용할 수 있다**
		- combinator.sc
### 펼치면서 연관시키기
- flatMap
	- map 호출 후 flatten 메서드 호출한 것과 정확히 같은 역할
### 걸러내기
- [TraversableLike](https://www.scala-lang.org/api/2.12.3/scala/collection/TraversableLike.html)에 존재
- 종류
	- drop
	- dropWhile
	- exists
	- filter
	- filterNot
	- find
	- forall
	- partition
	- take
	- takeWhile
### 접기와 축약시키기
- 축약(reduce)는 초깃값을 지정 X
	- 원소 사이에서 가장 가까운 공통의 부모타입만 반환 가능
- 접기(folding)은 초깃값으로 시작
	- 축약에 비해 최종 결과가 자유롭다
- 종류
	- fold
	- foldLeft
	- foldRight
	- `/:`
	- `:\`
	- reduce
	- reduceLeft
	- reduceRight
	- optionReduce
	- reduceLeftOption
	- reduceRightOption
	- aggregate
	- scan
	- scanLeft
	- scanRight
	- product
	- mkString
- 주의 사항
	- fold와 reduce는 특정 순회 순서를 보장하지 않는다
		- 예를 들어 List의 경우 reduce는 reduceLeft다
		- 따라서 결합법칙, 교환법칙이 성립하는 연산자를 사용해야 한다
- 왼쪽 순회와 오른쪽 순회
	- ```
		((((1 + 2) + 3) + 4) + 5) // = 15 (reduceLeft의 경우)
		(1 + (2 + (3 + (4 + 5)))) // = 15 (reduceRight의 경우)
	- left 연산은 꼬리 호출 재귀이기 때문에 최적화로 성능 향상을 얻을 수 있다
		- `fold-impl.sc`
	- right 연산은 뒷부분을 평가하지 않음으로써 무한한 지연 스트림을 처리할 수 있다
	

## 6.10 콤비네이터 : 가장 뛰어난 소프트웨어 컴포넌트 추상화
- 단순한 컬렉션으로부터 고차함수로 함수를 조합해 사용하여 추상화를 이뤄낸다
- Object study 7장
	- src/main/java/org/coukie/object/chapter07/origin.md
- 클래스가 아닌 타입과 함수를 모델링
## 6.11 복사에 드는 비용은 어떤가?
- **영속적인 데이터 구조**를 이용하여 immutable을 변경 대신 복사 후 새로 생성할 때 효율적으로 복사한다
- Vector는 Tree를 영속적인 데이터 구조를 이용하여 구현했다
	- https://hypirion.com/musings/understanding-persistent-vector-pt-1
- 더 자세한 내용이 필요하다면
	- Purely Functional Data Structures
	- Pearls of Functional Algorithm Design
	- Algorithms: A Functional Programming Approach
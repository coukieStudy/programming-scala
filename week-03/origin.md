# Chapter 3

## 3.1 연산자 오버로딩?

대부분의 연산자는 실제로는 메서드이다. 두 숫자 사이의 + 도 메서드이다. Java에서 primitive 타입도 스칼라에서는 일반적인 객체이다.
인자가 하나뿐인 메서드의 경우 마침표와 괄호를 생략할 수 있기 때문에 다음 두 메서드는 동일하다.

```scala
1 + 2
1.+(2)
```

인자가 없는 메서드는 항상 마침표 없이 호출할 수 있으며 인자가 없는 메서드의 경우 괄호를 생략할 수 있다. 따라서 다음과 같은 문법이 가능하다.

```scala
1 toString
```

### 식별자

식별자(identifier)는 메서드나 타입, 변수 등에 쓰이는 이름을 지칭한다. 식별자에 적용되는 규칙은 다음과 같다.

* 예약어는 사용 불가
* 문자나 밑줄로 시작하고, 그 뒤에 글자/숫자/밑줄/달러 기호가 올 수 있다
* '\_'(underscore) 가 식별자 중에 들어가면, 이는 공백을 만날 때 까지 모든 문자를 식별자의 일부분으로 사용하라는 의미이다. 따라서
  val xyz\_++= = 1은 변수 xyz\_++= 에 1 을 대입하라는 의미이다.
* 밑줄 다음에 연산자문자가 온다면, 이를 글자와 숫자와 섞어서 사용하면 안된다. abc\_-123 이 있을 때, abc\_-123 이라는 변수일 수도 있고, abc\_ 에서 123 을 빼라는 의미가 될 수도 있기 때문이다.
* 식별자가 연산자문자로 시작한다면, 식별자의 나머지 문자도 연산자여야 한다.
* ` (back quote 혹은 back tick) 2개 사이의 임의의 문자열을 식별자로 사용할 수 있다.



> 제목이 연산자 오버로딩인데 연산자 오버로딩에 대한 내용은 없으므로.. 
> https://docs.scala-lang.org/tour/operators.html#defining-and-using-operators



## 3.2 빈 인자목록이 있는 메서드

1. 메서드가 아무 매개변수도 취하지 않는다면 괄호없이 정의할 수 있다.
2. 정의에 괄호가 없는 메서드는 호출할 때 괄호를 사용할 수 없다 - List.size 의 정의에는 괄호가 없으므로 List(1, 2, 3).size 만 가능하고 List(1, 2, 3).size() 는 컴파일에러
3. 정의에 빈 괄호가 있다면 호출할 때 괄호를 넣거나 생략하는 방식 중 하나를 선택할 수 있다. - Java.lang.String의 length 는 괄호가 포함되어 있으므로 "hello".length / "hello".length() 모두 가능

관례상 컬렉션의 크기 연산과 같이 부수 효과가 없는 메서드는 괄호를 생략하고, 메서드에 부수효과가 있거나 상태 변경이 일어날 수 있는 경우 괄호를 더하는 방식을 선택한다.

괄호 생략으로 인해서 다음과 같은 메서드 연산이 가능하다.

```scala
def isEven(n: Int) = (n % 2) == 0
List(1, 2, 3, 4).filter((i: Int) => isEven(i)).foreach((i: Int) => println(i))
List(1, 2, 3, 4).filter(i => isEven(i)).foreach(i => println(i))
List(1, 2, 3, 4).filter(isEven).foreach(println)
List(1, 2, 3, 4) filter isEven foreach println
```



### 3.3 우선순위 규칙

1. 모든 글자

2. \|

3. \^

4. \&

5. \< \>

6. \= \!

7. \:

8. \+ \-

9. \* / %

10. 다른 모든 특수문자

    

    = 을 대입에 사용할 경우 가장 낮은 우선순위가 된다

왼쪽으로 결합하는 메서드를 연속적으로 호출하는 경우 이들은 왼쪽에서 오른쪽으로 묶인다. 이름이 : (colon) 으로 끝나는 메서드는 항상 오른쪽으로 묶인다.

```scala
val list = List('b', 'c', 'd')
'a' :: list
// result = List(a, b, c, d)
list.::'a'
```

### 3.4 DSL - 20장에서

### 3.5 IF 문

기본적인 동작은 java의 if문과 비슷하다. 조건식을 평가해서 그 식이 참이되면 다음 블록을 평가하고 참이 아니면 다음번 가지를 검사하거나 실행한다.

스칼라는 if 식의 결과값을 다른 변수에 저장할 수 있다.

```scala
val configFile = new java.io.File("someFile.txt")
val configFilePath = if (configFile.exists()) {
  configFile.getAbsolutePath()
} else {
  configFile.createNewFile()
  configFile.getAbsolutePath()
}
```

if 문의 리턴타입은 모든 가지의 최소 상위 바운드이다(각 가지의 모든 잠재적인 값에 대응하는 가장 가까운 부모 타입). 
if 문 자체가 리턴값이 있는 식이기 때문에 삼항연산자는 지원하지 않는다.

### 3.6 For

#### 3.6.1 for loop

Java의 for (Type t : List) 와 비슷

```scala
val dogBreeds = List("Doberman", "Yorkshire Terrier", "Dachshund", "Scottish Terrier", "Great Dane", "Portuguese Water Dog")
for (breed <- dogBreeds)
	println(breed)
```

#### 3.6.2 제네레이터 식

breed <- dogBreeds 가 제네리어티식이다. 왼쪽 화살표 연산자는 컬렉션을 순회하기 위해 사용한다. Range를 사용할 수도 있다.

```scala
for (i <- 1 to 10) println(i)
```

#### 3.6.3 가드

컬렉션에서 특정 값만 남기기 위한 식을 의미한다

```scala
for (breed <- dogBreeds if breed.contains("Terrier"))
	println(breed)
```

#### 3.6.4 yield

식이 여러개 들어가면 관례상 괄호 대신 중괄호를 사용하며, for문의 결과를 다시 변수에 저장하기 위함

```scala
val filteredBreeds = for {
  breed <- dogBreeds
  if breed.contains("Terrier") && !breed.startsWith("Yorkshire")
} yield breed
```

이 때 filteredBreeds 의 타입은 List[String] 이다. 원래의 dogBreeds가 List[String] 이었기 때문.

#### 3.6.5 확장 영역과 값 정리

```scala
val dogBreeds = List(Some("A"), None, Some("B"), None, None, Some("C"))

for {
  breedOption <- dogBreeds
  breed <- BreedOption
  println(breed)
}

for {
  Some(breed) <- dogBreeds
  println(breed)
}
```

breedOption <- dogBreeds 을 통해 뽑아낸 각 원소는 Option이다. 

breed <- BreedOption 에서는 Option에서 값을 꺼낸다. 원래 None 에서 값을 뽑아내면 예외를 던지나, for 내장에서는 None 이 아닌 경우에 대해서만 처리한다. if breedOption != None 이 추가된 것과 동일하다.

패턴 매칭을 사용하면 dogBreed가 Some 인 경우에만 추출하여 바로 품종 이름을 출력할 수 있게 된다.



왼쪽 화살표(<-) 는 컬렉션(Option 포함)을 대상으로 반복하면서 값을 가져와야 하는 경우에 사용하고 등호(=)는 반복과 관계없이 값을 대입하는 데에 사용된다. For 의 첫번째 식은 반드시 왼쪽 화살표를 사용한 추출이나 반복이어야 한다.

스칼라의 for는 break, continue 와 같은 문법을 지원하지 않는다. 하지만 다른 기능들을 활용하면 이런 문법들이 필요가 없다.

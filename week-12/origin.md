# 9. Trait

## 9.1 java interface

- 자바 implements 여러개 가능 -> 준비 코드 필요함.
- default method 등장, 스칼라 trait와 비슷하게 작동
- 정적 필드만 정의 가능, 스칼라 trait는 인스턴스 수준의 필드 정의 가능

## 9.2 mixin으로서의 trait

https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui/ButtonCallbacks.scala

콜백처리와 버튼논리를 분리하자. -> Subject와 Observer로 분리하자. (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/observer/observer.scala)
- Observer: 상태 변경을 전달받고 싶은 고객을 위한 트레이트 (자바8 이전 인터페이스와 동일)
- Subject: 관찰자에게 보낼 주체


- 버튼은 단순화된다. (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui/button.scala)
- ObservableButton (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui/ObservableButton.scala)
- ButtonCountObserver (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui/button-count-observer.sc)


## 9.3 Trait 쌓기

- 재사용성을 높이고 trait를 한번에 하나 이상 사용하기(쌓기)
- trait 호출 순서 : <-
- trait Clickable (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui2/clickable.scala)
- class Button with Clickable (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui2/button.scala)
- trait VetoableClicks (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui2/VetoableClicks.scala)
- trait ObservableClicks (https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/ui2/ObservableClicks.scala)

```
// new Button("Click Me!") with VetoableClicks with ObservableClicks

def ObservableClicks.click() = {
  if (count < maxAllowed) { // super.click => VetoableClicks.click
    count += 1
    {
     updateUI() // super.click => Clickable.click
    }
  }
  notifyObservers(this)
}

// new Button("Click Me!") with ObservableClicks with VetoableClicks

def VetoableClicks.click() = {
  if (count < maxAllowed) {
    count += 1 { // super.click => ObservableClicks.click
    {
      updateUI() // super.click => Clickable.click
    }
    notifyObservers(this)
    }
  }
}
```

- 트레이트가 너무 많으면 컴파일 시간이 오래걸린다...


## 9.4 Trait 만들기

- 생성은 : ->
- https://github.com/deanwampler/programming-scala-book-code-examples/blob/release-2.5.1/src/main/scala/progscala2/traits/trait-construction.sc

```
Creating C12:
  in Base12: b = null
  in Base12: b = Base12
  in T1: x = 0
  in T1: x = 1
  in T2: y = null
  in T2: y = T2
  in C12: c = null
  in C12: c = C12
After Creating C12
```

## 9.5 Class vs Trait

- 믹스인으로서의 트레이트는 부가적인 동작의 경우가 가장 적당.
- 부모클래스로 사용하려고할때는 Class로 정의하 것을 고려해보자.

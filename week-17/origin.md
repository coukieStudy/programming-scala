# 14장 스칼라 타입 시스템 1

# 14.1 매개변수화한 타입

## 14.1.1 변성 표기(Variance annotation)

List[+A]와 같은 표기는 List가 A라는 이름으로 표현되는 하나의 타입을 매개변수로 받는다는 의미

- + 변성 표기(공변성): String이 AnyRef의 서브타입이기 때문에 List[String]이 List[AnyRef]의 서브타입으로 간주된다는 의미
- - 변성 표기(반공변성): 공변성의 반대되는 개념.

## 14.1.2 타입 생성자(Type constructor)

어떤 클래스의 인스턴스를 만들어낼 때, 인스턴스 생성자를 사용하는 것처럼

구체적인 타입을 만들어낼 때, 매개변수화한 타입을 사용한다는 사실을 반영

e.g. List는 List[String], List[Int]의 타입 생성자

## 14.1.3 타입 매개변수의 이름

1. 컨테이너 원소 타입과 같이 아주 일반적인 타입 매개변수에 대해서는 A, B, T1, T2와 같이 한 두자로 된 이름을 사용. 실제 원소 타입의 이름은 컨테이너와 아무런 관계가 없음
2. 하부 컨테이너와 밀접한 연관이 있는 타입에 대해서는 좀 더 서술적인 이름을 사용하라. e.g. scala trait

# 14.2 타입 바운드

매개변수화한 타입이나 메서드를 정의할 때, 타입 매개변수에 대해 구체적인 바운드를 지정할 필요가 있을 때도 있다.

## 14.2.1 상위 타입 바운드(Upper type bound)

어떤 타입이 다른 특정 타입의 서브타입이어야 한다는 제약을 가하는 것

```scala
implicit def refArrayOps[T <: AnyRef](xs: Array[T]): ArrayOps[T] = new ArrayOpsl.ofRef[T](xs)
```

- A <: AnyRef라는 타입 매개변수는 'AnyRef의 서브타입인 어떤 타입 A'
- 자바에서는 extends

## 14.2.2 하위 타입 바운드(Lower type bound)

한 타입이 다른 타입의 슈퍼타입이어야만 한다는 표시

- A >: AnyRef라는 타입 매개변수는 'AnyRef의 슈퍼타입인 어떤 타입 A'
- 자바에서는 super

```scala
//Option (하위 타입 바운드를 사용)
sealed abstract class Option[+A] extends Product with Serializable {
	@inline final def getOrElse[B >: A](default: => B): B =
	    if (isEmpty) default else this.get
..
}

//예제
class Parent(val value: Int) {                   // <1>
  override def toString = s"${this.getClass.getName}($value)" 
}
class Child(value: Int) extends Parent(value)

val op1: Option[Parent] = Option(new Child(1))   // <2>     Some(Child(1))
val p1: Parent = op1.getOrElse(new Parent(10))   // 결과: Child(1)

val op2: Option[Parent] = Option[Parent](null)   // <3>     None
val p2a: Parent = op2.getOrElse(new Parent(10))  // 결과: Parent(10)
val p2b: Parent = op2.getOrElse(new Child(100))  // 결과: Child(100)

val op3: Option[Parent] = Option[Child](null)    // <4>     None
val p3a: Parent = op3.getOrElse(new Parent(20))  // 결과: Parent(20)
val p3b: Parent = op3.getOrElse(new Child(200))  // 결과: Child(200)
```

```scala
//Option (하위 타입 바운드를 사용하지 않은 예시)
case class Opt[+A](value: A = null) {
  def getOrElse(default: A): A = if (value != null) value else default 
}
//Error: covariant type A occurs in contravariant position in type A of value default
//에러가 발생하는 이유: 인자는 반공변적이어야 하고, 반환은 공변적이어야 하기 때문(즉, 상위 -> 하위)

class Parent(val value: Int) {                             // <1>
  override def toString = s"${this.getClass.getName}($value)" 
}
class Child(value: Int) extends Parent(value)
```

```scala
//Option (하위 타입 바운드/공변성을 사용하지 않은 예시)
case class Opt[A](value: A = null) {
  def getOrElse(default: A): A = if (value != null) value else default 
}

class Parent(val value: Int) {                             // <1>
  override def toString = s"${this.getClass.getName}($value)" 
}
class Child(value: Int) extends Parent(value)

val p4: Parent = Opt(new Child(1)).getOrElse(new Parent(10)) //오류: Opt(new Child(1))가 Opt[Child] type이기에
val p5: Parent = Opt[Parent](null).getOrElse(new Parent(10))
val p6: Parent = Opt[Child](null).getOrElse(new Parent(10)) //오류: Opt(new Child(1))가 Opt[Child] type이기에
```

**Function[-T1, -T2, +R]와 같이 함수 인자가 반공변성이고, 반환값이 공변성이어야 하는 이유**

<6>번이 가능하다고 가정을 해보면 다음과 같은 일이 발생한다.

- 호출하는 쪽에서 C 객체를 보냈을 때, C에는 없는데 CSub에 정의되어 있는 메서드를 호출하려고 하는 게 그런 예시이다.

```scala
class CSuper                { def msuper() = println("CSuper") }       // <1>
  class C      extends CSuper { def m()      = println("C") }
  class CSub   extends C      { def msub()   = println("CSub") }

  var f: C => C = (c: C)      => new C             // <2>
      f         = (c: CSuper) => new CSub          // <3>
      f         = (c: CSuper) => new C             // <4>
      f         = (c: C)      => new CSub          // <5>
      f         = (c: CSub)   => new CSuper        // <6> 컴파일 오류!
```

## 14.3 맥락 바운드(Context Bound)

```scala
case class MyList[A](list: List[A]) {
  def sortBy1[B](f: A => B)(implicit ord: Ordering[B]): List[A] =
    list.sortBy(f)(ord) //[1]

  def sortBy2[B : Ordering](f: A => B): List[A] =
    list.sortBy(f)(implicitly[Ordering[B]]) //[2]
}

val list = MyList(List(1,3,5,2,4))

list sortBy1 (i => -i)
list sortBy2 (i => -i)
```

[2]는 Ordering[B]라는 타입의 암시적 매개변수를 넘기는 것과 동일

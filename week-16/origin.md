# 가시성 규칙
## 가시성 규칙의 기본
- 보통 public 접근자는 오브젝트의 사용자들이 보고 사용할 것들에 사용하며, 그 타입의 추상화된 모습을 구성한다.
- 객체지향 디자인에서는 필드를 private이나 protected로 두고 메서드를 통해 접근하도록 하는 것이 컨벤션이다. 여기엔 두 가지 이유가 있는데, 하나는 필드를 immutable하게 함으로써 통제되지 않는 변경을 방지하는 것이고, 둘째는 특정 필드를 추상화가 아닌 구현의 일부로 두기 위함이다.
- 단일 접근 원칙 (Uniform Access Principle): 클래스가 제공하는 어떤 값이 저장된 값이든 계산된 값이든 같은 방식으로 제공되어야 한다. 사용자가 구현을 신경쓰지 않을 수 있도록 해야 한다.
- 타입의 사용자는 두 종류이다.
	- 파생 타입 (합성, 상속 등) - 보통 더 많은 접근을 필요로 한다.
	- 타입의 인스턴스가 사용되는 코드
- 스칼라의 가시성 규칙은 자바와 비슷하지만 더 일관적이고 더 유연하다.
## public 가시성
- 스칼라에서는 public이 default이다. 자바에서 default 가시성이 package private인 것과 다르다.
- 따로 public 키워드가 없다.
- 기본적으로 자바의 public과 동일하다.
## protected 가시성
- protected 가시성은 파생 타입에게 접근 권한을 제공한다.
- protected 멤버는 해당 멤버를 정의한 타입 및 파생 타입에만 보인다.
- protected 타입은 동일 및 하위 패키지에서만 보인다.
- 자바의 경우 protected 멤버가 동일 및 하위 패키지에서 모두 보이며, protected 클래스 선언은 불가능하다.
## private 가시성
- private 가시성은 구현을 완전하게 감춘다.
- private 멤버는 그 멤버를 정의한 타입에만 보인다.
- private 타입은 동일 및 하위 패키지에서만 보인다.
- 기본적으로 자바의 private과 동일하다 (단 자바에선 private 클래스 선언이 불가능하다).
## Scoped private / protected 가시성
- 스칼라의 scoped private / protected 선언은 가시성 범위를 좀 더 섬세하게 조정할 수 있도록 한다.
- `private[X]`는 X라는 scope 안에서 private하다는 의미이다.
- private[this] 가시성
	- private[this] 멤버는 동일한 인스턴스 안에서만 보인다. 같은 타입의 다른 인스턴스에서는 보이지 않는다. 
	- private[this] 타입은 자바의 package private 와 동일한 가시성을 제공하며, 서브클래스는 private 혹은 private[this] 가시성으로만 선언할 수 있다.
- private[T] 가시성
	- T가 타입일 경우 private[T] 멤버는 타입 T 안에서만 보인다. 따라서 타입 T의 nested 클래스의 필드를 private[T]로 선언하면 해당 멤버는 nested 클래스 밖이더라도 타입 T 안에서면 보인다.
	- T가 scope일 경우 private[T] 멤버/타입은 scope T 안에서만 보인다.
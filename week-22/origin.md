# 24장 메타프로그래밍 메크로와 리플렉션

Meta programming은 metadata를 이용해서 다른 program을 작성하거나 조작하는 프로그램을 만드는 것이다. 아래는 실제 코드에서 metaprogramming이 사용되는 사례이다.

> 1) Static data that can be pre-computed or pre-generated at compile time
> 2) Eliminate boiler-plate code that cannot be abstracted in functions for DRYness sake
> - Think Aspects in AOP
> - Stereotypes in Spring
> - lombok?
> ... etc..
> 

여러 metaProgramming 기법들

- 타입 성찰(Type Introspection): 타입에 대한 정보를 가져오는 기능들을 타입성찰이라고 한다. 언어마다 개별적으로 구현되어있으며, java는 reflection API등을 통해 이를 가져올 수 있다.

```java
// Java 코드
// Class는 Invariant하기 때문에 `Class<?>`로 키타입을 설정해야한다.
// 여기서 ?는 타입이 일관적이지 않음을 의미한다.
HashMap<Class<?>, Integer> call_count = new HashMap<>();

// 인수로 받은 object를 타입별로 세는 함수
void increaseCallCount(Object object) {
  Integer integer = call_count.getOrDefault(object.getClass(), 0);
  call_count.put(object.getClass(), integer + 1);
}
```

- Reflection: 컴파일 타임에만 존재하던 정보를 런타임에서도 제공해주는 것
    - JVM의 경우 타입에 대한 정보를 남겨두어, 런타임에 Java의 Class<T>를 통해 타입 이름/클래스 내 메소드들, 필드들 접근 가능
    - 단점: 런타임에 오류 잡기가 어렵고, 성능상 이슈가 있다.

```java
public final class Class<T> {
  // 타입 이름을 가져오는 메소드
  public String getTypeName() {
    /* ... */
  }

  // 필드 정보들을 가져오는 메소드
  public Field[] getFields() {
    /* ... */
  }

  // 메소드 정보들을 가져오는 메소드
  public Method[] getDeclaredMethods() {
    /* ... */
  }

  /* ... */
}
```

- Annotation: 추가적인 정보를 더 제공해서 컴파일러의 동작을 바꾸거나 하는 등 메타프로그래밍을 좀 더 조절할 수 있도록 한다.
    - @Target(ElementType.Field): Annotation이 Field에만 붙을 수 있음
    - @Rentention(RententionPolicy.RUNTIME)
        - SOURCE: 컴파일 타임까지. e.g. Lombok getter setter (Code generation)
        - CLASS: 클래스 파일까지 e.g. class 파일만 존재하는 라이브러리 같은 경우에도 타입체커, IDE 부가기능 등을 사용할수 있으려면 CLASS 정책이 필요
        - RUNTIME: 런타임까지이기에 Reflection으로 제공된다는 의미 e.g. Spring Controller, service annotation

```java
enum CasingType { camelCase, snake_case }

@Target(ElementType.Field)
@Rentention(RententionPolicy.RUNTIME)
public @interface Casing {
  CasingType casingType default CasingType.camelCase;
}
```

ref. [https://jeong-pro.tistory.com/234](https://jeong-pro.tistory.com/234)

- Template metaprogramming: 컴파일 타임에 코드를 이용하여 메타프로그래밍하는 방법
    - printValue parameter로 value가 없는 타입에 대해서는 컴파일 에러
    - 컴파일러가 템플릿 코드를 생성할 때 제공된 타입 T의 필드들과 그 이름, 그리고 필드들의 타입을 알고 있고 그러한 정보들을 이용해 컴파일이 성공하는지 확인하며 코드를 생성

```cpp
template<typename T>
void printValue(T t) {
  cout << t.value << endl;
}

// A.value는 int 타입
struct A {
  int value;
};

// B.value는 float 타입
struct B {
  float value;
};

A a = { 5 };
B b = { 10.5f };
printValue(a);
printValue(b);
```

 

- 매크로: template metaprogramming처럼 타입을 치환해서 생성할 수 있을 뿐더러, 코드의 모든 부분들을 치환할 수 있다.

```cpp
#define DEF_PRINT_FIELD(T, fieldName)  \
  void print_##fieldName(T t) {        \
    cout << t.fieldName << endl;       \
  }

struct Human {
  string name;
  int age;
  string role; 
};

DEF_PRINT_FIELD(Human, name)
// 아래 코드가 생성된다.
//  void print_name(T t) {
//    cout << t.name << endl;
//  }

DEF_PRINT_FIELD(Human, age)
// 아래 코드가 생성된다.
//  void print_age(T t) {
//    cout << t.age << endl;
//  }

DEF_PRINT_FIELD(Human, role)
// 아래 코드가 생성된다.
//  void print_role(T t) {
//    cout << t.role << endl;
//  }

Human human = { "mango", 23, "software engineer" };

print_name(human);
print_age(human);
print_role(human);
```

## Scala의 메타프로그래밍

- 매크로 기능을 통해 컴파일 시점에 발생
- Enable us to utilize the compiler’s Abstract Syntax Tree API to implement compile-time reflection.

## Reference

- [https://www.baeldung.com/java-reflection](https://www.baeldung.com/java-reflection)
- [https://www.youtube.com/watch?v=0b0hrd6k1Zs](https://www.youtube.com/watch?v=0b0hrd6k1Zs)
- [https://tech.devsisters.com/posts/programming-languages-3-metaprogramming/](https://tech.devsisters.com/posts/programming-languages-3-metaprogramming/)

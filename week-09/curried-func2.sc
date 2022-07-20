def cat1(s1: String)(s2:String) = s1 + s2
def cat2(s1: String) = (s2: String) => s1 + s2 // 커링한 함수

val cat2hello = cat2("Hello ") // _가 없다
cat2hello("World!")

// curried 메서드를 이용해 커링하기
def cat3(s1: String, s2: String) = s1 + s2
val cat3Curried = (cat3 _).curried
cat3Curried("Hello ")("world")

val f1: String => String => String = (s1: String) => (s2: String) => s1 + s2
val f2: String => (String => String) = (s1:String) => (s2: String) => s1 + s2

// 두 함수는 동일하다
f1("Hello ")("World!")
f2("Hello ")("World!")

// 언커링
val cat3Uncurried = Function.uncurried(cat3Curried)
cat3Uncurried("Hello", " World")
val ff1 = Function.uncurried(f1)
ff1("Hello", " World")
def cat1(s1:String)(s2: String) = s1 + s2

val hello = cat1("Hello ")  _ // 뒤의 언더 스코어로 부분 적용 함수임을 밝힌다(eta conversion)
hello("World!")

cat1("Hello ")("World!")

// 에러를 만든다
// val error = cat1("Hello ")
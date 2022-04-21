import progscala2.typelessdomore.shapes.{Circle, Point}

val p1 = new Point(x= 3.3, y= 4.4)
val p2 = p1.copy(y=6) //shallow copy

p1.x = 1.0

p1.x
p2.x

/**
 * 두 인자 목록을 받는 장점
 * 첫번째 장점. 구문적 편의
 */
val s = new Circle(Point(0.0, 0.0), 1.0)
s.draw(Point(1.0, 1.0)) {
  str => println(s"$str") //s를 붙여야지 str 출력 가능
}

/**
 * 두번째 장점
 */

def m1[A](a: A, f: A => String) = f(a)
def m2[A](a: A)(f: A => String) = f(a)

m1(100, i => s"$i + $i") //첫번째 parmeter로 두번째를 추론하지 못한다.
m2(100)(i => s"$i + $i")
# 스칼라 DSL
- DSL vs GPL
	- DSL (Domain Specific Language): 특정 도메인에서만 사용되는 프로그래밍 언어
	- GPL (General Purpose Language): 범용 프로그래밍 언어 (C, Java, ...)
	- 우리가 자주 쓰는 DSL 예: HTML, RegEx, Gradle
		```
		tasks.register('hello') {
		    doLast {
		        println 'Hello world!'
		    }
		}
		```
- DSL의 장점과 단점
	- 장점: 캡슐화, 생산성 향상, 도메인 전문가와의 의사소통 향상
	- 단점: 만들기 어려움, 유지보수하기 어려움
- 내부 DSL vs 외부 DSL
	- 내부 DSL: 하나의 특정 GPL을 기반으로 구현되는 DSL (ex. JPQL, React), 더 만들기 쉬우나 기반 GPL의 제약을 받는다.
	- 외부 DSL: 자체 문법과 구문분석기를 필요로 하는 별개의 언어 (ex. HTML, SQL), 만들기 어려운 대신 한계가 없다.

## 예제: 스칼라를 위한 XML DSL
- 스칼라에서 XML 지원은 별도의 라이브러리인 scala-dsl (https://github.com/scala/scala-xml) 을 이용한다. 이 라이브러리를 임포트하면 DSL을 사용해 XML 구분 분석을 할 수 있다.
- 이 DSL은 내부 DSL에 속한다.
```
// src/main/scala/progscala2/dsls/xml/reading.sc
import scala.xml._                                                   // <1>

val xmlAsString = "<sammich>...</sammich>"                           // <2>
val xml1 = XML.loadString(xmlAsString)

val xml2 =                                                           // <3>
<sammich>
  <bread>wheat</bread>
  <meat>salami</meat>
  <condiments>
    <condiment expired="true">mayo</condiment>
    <condiment expired="false">mustard</condiment>
  </condiments>
</sammich>

for {                                                                // <4>
  condiment <- (xml2 \\ "condiment")
  if (condiment \ "@expired").text == "true"
} println(s"the ${condiment.text} has expired!")

def isExpired(condiment: Node): String =                             // <5>
  condiment.attribute("expired") match {
    case Some(Nil) | None => "unknown!"
    case Some(nodes) => nodes.head.text
  }

xml2 match {                                                         // <6>
  case <sammich>{ingredients @ _*}</sammich> => {
    for {
      condiments @ <condiments>{_*}</condiments> <- ingredients
      cond <- condiments \ "condiment"
    } println(s"  condiment: ${cond.text} is expired? ${isExpired(cond)}")
  }
}
```
- `xml \ "foo"`: 자손 노드 (child node) 추출
- `xml \\ "foo"`: 후손 노드 (descendant node) 추출
- `xml \ "@foo"`: XPath를 이용한 애트리뷰트 추출
- `case <foo>{bar @ _*}</foo>`: XML 리터럴에 패턴 매칭

## 스칼라로 내부 DSL 만들기
- 스칼라의 문법적 특징이 내부 DSL 구현을 지원한다.
	- 명명 규칙이 유연하다 (기호 등)
	- 중위 및 후위 표기법을 사용할 수 있다
	- implicit import
	- 동적 메소드 호출
	- 고차 함수, 이름에 의한 호출, lazy evaluation
	- 자기 타입 표기 (self type annotation)
	- 매크로
- 급여 계산 애플리케이션을 위한 DSL 만들기
	- 타입 정의
		```
			// src/main/scala/progscala2/dsls/payroll/common.scala
		package progscala2.dsls.payroll

		object common {
		  sealed trait Amount { def amount: Double }                         // <1>

		  case class Percentage(amount: Double) extends Amount {
		    override def toString = s"$amount%"
		  }

		  case class Dollars(amount: Double) extends Amount {
		    override def toString = s"$$$amount"
		  }

		  implicit class Units(amount: Double) {                             // <2>
		    def percent = Percentage(amount)
		    def dollars = Dollars(amount)
		  }

		  case class Deduction(name: String, amount: Amount) {               // <3>
		    override def toString = s"$name: $amount"
		  }

		  case class Deductions(                                             // <4>
		    name: String,
		    divisorFromAnnualPay: Double = 1.0,
		    var deductions: Vector[Deduction] = Vector.empty) {

		    def gross(annualSalary: Double): Double =                        // <5>
		      annualSalary / divisorFromAnnualPay
		    
		    def net(annualSalary: Double): Double = {
		      val g = gross(annualSalary)
		      (deductions foldLeft g) { 
		        case (total, Deduction(deduction, amount)) => amount match {
		          case Percentage(value) => total - (g * value / 100.0)
		          case Dollars(value) => total - value
		        }
		      }
		    }

		    override def toString =                                          // <6>
		      s"$name Deductions:" + deductions.mkString("\n  ", "\n  ", "")
		  }
		}
		```
	- DSL 구현
		```
		// src/main/scala/progscala2/dsls/payroll/internal/dsl.scala
		package progscala2.dsls.payroll.internal
		import scala.language.postfixOps                                     // <1>
		import progscala2.dsls.payroll.common._

		object Payroll {                                                     // <2>
		  
		  import dsl._                                                       // <3>

		  def main(args: Array[String]) = {
		    val biweeklyDeductions = biweekly { deduct =>                    // <4>
		      deduct federal_tax          (25.0  percent)
		      deduct state_tax            (5.0   percent)
		      deduct insurance_premiums   (500.0 dollars)
		      deduct retirement_savings   (10.0  percent)
		    }

		    println(biweeklyDeductions)                                      // <5>
		    val annualGross = 100000.0
		    val gross = biweeklyDeductions.gross(annualGross)
		    val net   = biweeklyDeductions.net(annualGross)
		    print(f"Biweekly pay (annual: $$${annualGross}%.2f): ")
		    println(f"Gross: $$${gross}%.2f, Net: $$${net}%.2f")
		  }
		}

		object dsl {                                                         // <1>

		  def biweekly(f: DeductionsBuilder => Deductions) =                 // <2>
		    f(new DeductionsBuilder("Biweekly", 26.0))

		  class DeductionsBuilder(                                           // <3>
		    name: String,
		    divisor: Double = 1.0,
		    deducts: Vector[Deduction] = Vector.empty) extends Deductions(
		      name, divisor, deducts) {

		    def federal_tax(amount: Amount): DeductionsBuilder = {           // <4>
		      deductions = deductions :+ Deduction("federal taxes", amount)
		      this
		    }

		    def state_tax(amount: Amount): DeductionsBuilder = {
		      deductions = deductions :+ Deduction("state taxes", amount)
		      this
		    }

		    def insurance_premiums(amount: Amount): DeductionsBuilder = {
		      deductions = deductions :+ Deduction("insurance premiums", amount)
		      this
		    }

		    def retirement_savings(amount: Amount): DeductionsBuilder = {
		      deductions = deductions :+ Deduction("retirement savings", amount)
		      this
		    }
		  }
		}
		```
	- 대충 "이런 식으로 구현할 수 있구나" 까지만 알고 넘어가자.
- 단점
	- 스칼라의 문법을 활용하는 데 너무 의존한다. 그 문법에서 벗어나면 코드가 쉽게 망가진다. (중위 표기법 등)
	- 문법 규칙이 임의적이다. (스칼라 프로그래머가 아니면 사소한 것들을 이해하기 힘듬)
	- 오류 메시지를 이해하기 힘들다 (도메인 관련 에러가 아닌 스칼라 에러가 나타나기 때문)
	- 잘못된 사용을 막지 못한다. (의도하지 않은 암시적 변환 사용 등)

## 파서 콤비네이터를 활용해 외부 DSL 만들기
- 별도 라이브러리인 스칼라 파서 콤비네이터 (https://github.com/scala/scala-parser-combinators) 를 활용해 외부 DSL을 만들 수 있다. 이를 사용하면 EBNF (Extended Backus-Naur Form)와 비슷한 표기를 사용해 구문분석기를 정의할 수 있다. (EBNF에 대해선 https://blog.naver.com/PostView.naver?blogId=kelly9509&logNo=222330672820 참고)
- 외부 DSL 구현
	```
	// src/main/scala/progscala2/dsls/payroll/parsercomb/dsl.scala
	package progscala2.dsls.payroll.parsercomb
	import scala.util.parsing.combinator._
	import progscala2.dsls.payroll.common._                              // <1>

	object Payroll {
	  
	  import dsl.PayrollParser                                           // <2>

	  def main(args: Array[String]) = {                                  // <3> 
	    val input = """biweekly {      
	      federal tax          20.0  percent,
	      state tax            3.0   percent,
	      insurance premiums   250.0 dollars,
	      retirement savings   15.0  percent
	    }"""
	    val parser = new PayrollParser                                   // <4> 
	    val biweeklyDeductions = parser.parseAll(parser.biweekly, input).get

	    println(biweeklyDeductions)                                      // <5>
	    val annualGross = 100000.0
	    val gross = biweeklyDeductions.gross(annualGross)
	    val net   = biweeklyDeductions.net(annualGross)
	    print(f"Biweekly pay (annual: $$${annualGross}%.2f): ")
	    println(f"Gross: $$${gross}%.2f, Net: $$${net}%.2f")
	  }
	}

	object dsl {                                    

	  class PayrollParser extends JavaTokenParsers {                     // <1>

	    /** @return Parser[(Deductions)] */
	    def biweekly = "biweekly" ~> "{" ~> deductions <~ "}" ^^ { ds => // <2> 
	      Deductions("Biweekly", 26.0, ds)
	    }

	    /** @return Parser[Vector[Deduction]] */
	    def deductions = repsep(deduction, ",") ^^ { ds =>               // <3>
	      ds.foldLeft(Vector.empty[Deduction]) (_ :+ _)
	    }

	    /** @return Parser[Deduction] */
	    def deduction = federal_tax | state_tax | insurance | retirement // <4>

	    /** @return Parser[Deduction] */
	    def federal_tax = parseDeduction("federal", "tax")               // <5>
	    def state_tax   = parseDeduction("state", "tax")
	    def insurance   = parseDeduction("insurance", "premiums")
	    def retirement  = parseDeduction("retirement", "savings")

	    private def parseDeduction(word1: String, word2: String) =       // <6> 
	      word1 ~> word2 ~> amount ^^ { 
	        amount => Deduction(s"${word1} ${word2}", amount)
	      }

	    /** @return Parser[Amount] */
	    def amount = dollars | percentage                                // <7>

	    /** @return Parser[Dollars] */
	    def dollars = doubleNumber <~ "dollars" ^^ { d => Dollars(d) }

	    /** @return Parser[Percentage] */
	    def percentage = doubleNumber <~ "percent" ^^ { d => Percentage(d) }

	    def doubleNumber = floatingPointNumber ^^ (_.toDouble)
	  }
	}
	```
## 내부 DSL과 외부 DSL 비교
- 위에서 만들어 본 내부 DSL과 외부 DSL을 비교해보자.
- 내부 DSL로 작성한 코드
	```
	val biweeklyDeductions = biweekly { deduct =>                    
	  deduct federal_tax          (25.0  percent)
	  deduct state_tax            (5.0   percent)
	  deduct insurance_premiums   (500.0 dollars)
	  deduct retirement_savings   (10.0  percent)
	}
	```
- 외부 DSL로 작성한 코드
	```
	val input = """biweekly {      
	  federal tax          20.0  percent,
	  state tax            3.0   percent,
	  insurance premiums   250.0 dollars,
	  retirement savings   15.0  percent
	}"""
	```
- 내부 DSL을 사용하면 스칼라 언어를 사용하는 것과 다를 바 없어 자동완성, 문법 하이라이팅 등의 기능을 사용할 수 있다.
- 외부 DSL은 형식이 더 단순하며 구현이 더 쉽다. 또한 스칼라 문법에 의존하지 않아 더 자유롭고 잘못된 사용도 막을 수 있다.
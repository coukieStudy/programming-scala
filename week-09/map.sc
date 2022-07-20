val stateCapitals = Map(
  "Alabama" -> "Montgomery",
  "Alaska" -> "Juneau",
  "Wyoming" -> "Cheyenne")

val lengths = stateCapitals map {
  kv => (kv._1, kv._2.length)
}

val caps = stateCapitals map {
  case (k, v) => (k, v.toUpperCase)
}

val stateCapitals2 = stateCapitals + (
  "Virginia" -> "Richmond")

val stateCapitals3 = stateCapitals2 + (
  "New York" -> "Albany", "Illinois" -> "Springfield"
)

stateCapitals + "Virginia" -> "Richmond" // 괄호를 없애면 + 연산자로 인해 Map이 String로 바뀐 후 tuple이 생성된다
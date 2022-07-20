val seq1 = Seq("Programming", "Scala") // List임
val seq2 = "People" +: "should" +: "read" +: seq1
val seq3 = "Programming" +: "Scala" +: Seq.empty // Nil과 같다
val seq4 = "Peple" +: "should" +: "read" +: Seq.empty
val seq5 = seq4 ++ seq3
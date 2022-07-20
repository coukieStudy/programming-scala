val list1 = List("Programming", "scala")
val list2 = "People" :: "should" :: "read" :: list1 // "read" :: list1 은 list.::(x) 다
val list3 = "Programming" :: "Scala" :: Nil
val list4 = "Peopel" :: "Should" :: "read" :: Nil
val list5 = list4 ++ list3

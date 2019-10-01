package edu.cmu.cs.vbc.scripts

object Median {
  private val all = List(
    "median/0cea42f9/003/", // fixed
    "median/0cdfa335/003/", // fixed
    "median/15cb07a7/003/", // filtered, no positive tests
    "median/1b31fa5c/000/", // fixed by GenProg
    "median/1bf73a9c/000/", // filtered, no positive tests
    "median/1bf73a9c/003/", // fixed by GenProg
    "median/30074a0e/000/", // no pos test
    "median/68eb0bb0/000/", // no pos test
    "median/9013bd3b/000/", // no pos test
    "median/90834803/003/", // no pos test
    "median/95362737/000/", // no pos test
    "median/95362737/003/", // no pos test
    "median/c716ee61/000/", // no pos test
    "median/c716ee61/001/", // no pos test
    "median/fcf701e8/000/", // no pos test
    "median/1c2bb3a4/000/",
    "median/2c155667/000/", // cannot fix
    "median/317aa705/000/", // todo: something wrong
    "median/317aa705/002/",
    "median/317aa705/003/",
    "median/36d8008b/000/",
    "median/3b2376ab/003/", // fixed
    "median/3b2376ab/006/", // fixed, (300, 3, 1)
    "median/3cf6d33a/007/",
    "median/48b82975/000/",
    "median/6aaeaf2f/000/",
    "median/6e464f2b/003/",
    "median/89b1a701/003/",
    "median/89b1a701/007/",
    "median/89b1a701/010/", // fixed, (300, 3, 1)
    "median/90834803/010/", // fixed, (300, 3, 1)
    "median/90834803/015/",
    "median/90a14c1a/000/", // method code too large sometimes
    "median/93f87bf2/010/",
    "median/93f87bf2/012/",
    "median/93f87bf2/015/",
    "median/9c9308d4/003/",
    "median/9c9308d4/007/",
    "median/9c9308d4/012/", // fixed, (300, 5, 1)
    "median/aaceaf4a/003/",
    "median/af81ffd4/004/",
    "median/af81ffd4/007/",
    "median/b6fd408d/000/",
    "median/b6fd408d/001/",
    "median/c716ee61/002/",
    "median/cd2d9b5b/010/",
    "median/d009aa71/000/",
    "median/d120480a/000/",
    "median/d2b889e1/000/",
    "median/d43d3207/000/",
    "median/d4aae191/000/",
    "median/e9c6206d/000/",
    "median/e9c6206d/001/",
    "median/fcf701e8/002/", // stuck for some reasons
    "median/fcf701e8/003/",
    "median/fe9d5fb9/000/",
    "median/fe9d5fb9/002/"
  )
  private val noPosSkip = List(
    "median/fcf701e8/000/", "median/c716ee61/001/", "median/c716ee61/000/",
    "median/95362737/003/", "median/95362737/000/", "median/90834803/003/",
    "median/9013bd3b/000/", "median/68eb0bb0/000/", "median/30074a0e/000/",
    "median/1bf73a9c/000/", "median/15cb07a7/003/"
  )
  val runnable: List[String] = all filterNot (x => noPosSkip contains x)
}

object Checksum {
  private val all = List(
    "checksum/08c7ea4a/006/",
    "checksum/08c7ea4a/007/",
    "checksum/08c7ea4a/010/",
    "checksum/08c7ea4a/011/",
    "checksum/2c155667/003/",
    "checksum/36d8008b/003/",
    "checksum/659a7300/003/",
    "checksum/a0e3fdae/000/",
    "checksum/a0e3fdae/005/",
    "checksum/e23b96b6/005/",
    "checksum/e9c74e27/000/"
  )
  val runnable: List[String] = all
}

object Digits {
  private val all = List(
    "digits/ca94e375/000/",
    "digits/ca94e375/002/",
    "digits/f227ed28/000/",
    "digits/317aa705/004/",
    "digits/317aa705/002/",
    "digits/48b82975/000/",
    "digits/d2b889e1/000/",
    "digits/d2b889e1/001/",
    "digits/d2b889e1/006/",
    "digits/d2b889e1/003/",
    "digits/d2b889e1/004/",
    "digits/d2b889e1/005/",
    "digits/387be36e/000/",
    "digits/387be36e/002/",
    "digits/c9d718f3/000/",
    "digits/c9d718f3/001/",
    "digits/65e02c1a/015/",
    "digits/65e02c1a/004/",
    "digits/07045530/000/",
    "digits/07045530/002/",
    "digits/cd2d9b5b/003/",
    "digits/a0e3fdae/003/",
    "digits/0cea42f9/000/",
    "digits/0cea42f9/002/",
    "digits/c5d8f924/003/",
    "digits/295afd89/000/",
    "digits/295afd89/003/",
    "digits/295afd89/002/",
    "digits/5b504b35/000/",
    "digits/bfad6d21/000/",
    "digits/bfad6d21/003/",
    "digits/bfad6d21/004/",
    "digits/bfad6d21/005/",
    "digits/e9c74e27/000/",
    "digits/d43d3207/000/",
    "digits/6e464f2b/000/",
    "digits/6e464f2b/003/",
    "digits/6e464f2b/004/",
    "digits/98d873cd/000/",
    "digits/98d873cd/001/",
    "digits/98d873cd/003/",
    "digits/98d873cd/004/",
    "digits/88394fc0/003/",
    "digits/88394fc0/004/",
    "digits/3214e9b0/000/",
    "digits/3214e9b0/003/",
    "digits/1391c9b1/000/",
    "digits/1391c9b1/002/",
    "digits/313d572e/000/",
    "digits/0cdfa335/007/",
    "digits/0cdfa335/006/",
    "digits/0cdfa335/004/",
    "digits/0cdfa335/005/",
    "digits/1b31fa5c/000/",
    "digits/1b31fa5c/002/",
    "digits/2af3c416/000/",
    "digits/d8b26284/000/",
    "digits/e79f832f/000/",
    "digits/e79f832f/002/",
    "digits/90a14c1a/000/",
    "digits/90a14c1a/004/",
    "digits/ca505766/003/",
    "digits/08c7ea4a/000/",
    "digits/08c7ea4a/001/",
    "digits/d5059e2b/000/",
    "digits/d767ad3b/007/",
    "digits/d120480a/000/",
    "digits/d120480a/001/",
    "digits/1c2bb3a4/000/",
    "digits/1c2bb3a4/003/",
    "digits/9013bd3b/001/",
    "digits/d6364e6e/000/",
    "digits/833bd42c/000/",
    "digits/f2997e14/000/",
    "digits/f2997e14/002/"
  )
  private val noPosSkip = List(
    "digits/d120480a/000/",
    "digits/313d572e/000/",
    "digits/1391c9b1/002/",
    "digits/1391c9b1/000/",
    "digits/98d873cd/000/",
    "digits/bfad6d21/000/",
    "digits/f227ed28/000/"
  )
  val runnable: List[String] = all.filterNot(x => noPosSkip contains x)
  val debug: List[String] = List(
    "digits/295afd89/003/"
  )
}

object Grade {
  private val all = List(
    "grade/ca94e375/000/",
    "grade/317aa705/000/",
    "grade/317aa705/001/",
    "grade/317aa705/003/",
    "grade/317aa705/004/",
    "grade/48b82975/000/",
    "grade/fe9d5fb9/003/",
    "grade/fe9d5fb9/004/",
    "grade/387be36e/000/",
    "grade/ee1f20cc/000/",
    "grade/c9d718f3/000/",
    "grade/c9d718f3/001/",
    "grade/c9d718f3/003/",
    "grade/c9d718f3/004/",
    "grade/c9d718f3/002/",
    "grade/07045530/002/",
    "grade/cd2d9b5b/013/",
    "grade/cd2d9b5b/007/",
    "grade/cd2d9b5b/009/",
    "grade/cd2d9b5b/008/",
    "grade/cd2d9b5b/006/",
    "grade/cd2d9b5b/010/",
    "grade/cd2d9b5b/003/",
    "grade/a0e3fdae/012/",
    "grade/a0e3fdae/010/",
    "grade/95362737/015/",
    "grade/95362737/013/",
    "grade/95362737/014/",
    "grade/95362737/018/",
    "grade/95362737/010/",
    "grade/0cea42f9/000/",
    "grade/0cea42f9/001/",
    "grade/0cea42f9/003/",
    "grade/0cea42f9/002/",
    "grade/36d8008b/000/",
    "grade/295afd89/010/",
    "grade/5b504b35/000/",
    "grade/92b7dd12/002/",
    "grade/b1924d63/000/",
    "grade/b1924d63/001/",
    "grade/b1924d63/003/",
    "grade/dc11fbc9/000/",
    "grade/93f87bf2/015/",
    "grade/89b1a701/003/",
    "grade/bfad6d21/000/",
    "grade/bfad6d21/001/",
    "grade/75c98d3d/003/",
    "grade/aaceaf4a/003/",
    "grade/d009aa71/000/",
    "grade/d009aa71/003/",
    "grade/30074a0e/004/",
    "grade/68ea5d34/000/",
    "grade/3cf6d33a/010/",
    "grade/6e464f2b/000/",
    "grade/98d873cd/004/",
    "grade/769cd811/000/",
    "grade/48925325/000/",
    "grade/48925325/010/",
    "grade/e23b96b6/000/",
    "grade/e23b96b6/001/",
    "grade/e23b96b6/003/",
    "grade/e23b96b6/004/",
    "grade/e23b96b6/002/",
    "grade/0cdfa335/003/",
    "grade/af81ffd4/007/",
    "grade/af81ffd4/000/",
    "grade/af81ffd4/009/",
    "grade/af81ffd4/001/",
    "grade/af81ffd4/002/",
    "grade/1b31fa5c/003/",
    "grade/3b2376ab/007/",
    "grade/d8b26284/000/",
    "grade/9c9308d4/007/",
    "grade/9c9308d4/000/",
    "grade/9c9308d4/003/",
    "grade/dccb1242/001/",
    "grade/1c2bb3a4/000/",
    "grade/1c2bb3a4/003/",
    "grade/9013bd3b/000/",
    "grade/f5b56c79/000/",
    "grade/f5b56c79/010/",
    "grade/cb243beb/000/",
    "grade/cb243beb/001/",
    "grade/d6364e6e/007/",
    "grade/833bd42c/003/",
    "grade/90834803/013/",
    "grade/90834803/009/",
    "grade/90834803/010/",
    "grade/b6fd408d/000/"
  )
  private val noPosSkip = List(
    "grade/9013bd3b/000/",
    "grade/9c9308d4/003/",
    "grade/9c9308d4/000/",
    "grade/9c9308d4/007/",
    "grade/30074a0e/004/",
    "grade/0cea42f9/002/",
    "grade/0cea42f9/001/",
    "grade/0cea42f9/000/",
    "grade/cd2d9b5b/003/",
    "grade/387be36e/000/"
  )
  val runnable: List[String] = all filterNot (x => noPosSkip contains x)
  val debug: List[String] = List("grade/d009aa71/003/")
}

object Smallest {
  private val all = List(
    "smallest/818f8cf4/003/",
    "smallest/818f8cf4/002/",
    "smallest/ea67b841/003/",
    "smallest/dedc2a7c/000/",
    "smallest/48b82975/000/",
    "smallest/48b82975/001/",
    "smallest/c9d718f3/000/",
    "smallest/c9d718f3/003/",
    "smallest/c868b30a/000/",
    "smallest/95362737/009/",
    "smallest/36d8008b/003/",
    "smallest/93f87bf2/000/",
    "smallest/5a568359/000/",
    "smallest/d009aa71/001/",
    "smallest/30074a0e/007/",
    "smallest/30074a0e/000/",
    "smallest/68eb0bb0/000/",
    "smallest/3cf6d33a/003/",
    "smallest/f8d57dea/000/",
    "smallest/88394fc0/007/",
    "smallest/88394fc0/006/",
    "smallest/88394fc0/003/",
    "smallest/88394fc0/002/",
    "smallest/769cd811/007/",
    "smallest/769cd811/009/",
    "smallest/769cd811/010/",
    "smallest/769cd811/003/",
    "smallest/769cd811/004/",
    "smallest/769cd811/002/",
    "smallest/2694af73/000/",
    "smallest/af81ffd4/000/",
    "smallest/1b31fa5c/003/",
    "smallest/3b2376ab/007/",
    "smallest/3b2376ab/008/",
    "smallest/3b2376ab/006/",
    "smallest/3b2376ab/003/",
    "smallest/15cb07a7/007/",
    "smallest/90a14c1a/000/",
    "smallest/90a14c1a/001/",
    "smallest/e9c6206d/000/",
    "smallest/9013bd3b/000/",
    "smallest/cb243beb/000/",
    "smallest/97f6b152/003/",
    "smallest/90834803/005/",
    "smallest/346b1d3c/010/",
    "smallest/346b1d3c/003/",
    "smallest/346b1d3c/005/",
    "smallest/346b1d3c/002/",
    "smallest/6aaeaf2f/000/",
    "smallest/6aaeaf2f/001/",
    "smallest/f2997e14/000/",
    "smallest/84602125/007/"
  )
  private val noPosSkip = List(
    "smallest/6aaeaf2f/000/",
    "smallest/346b1d3c/002/",
    "smallest/3b2376ab/003/",
    "smallest/3b2376ab/006/",
    "smallest/88394fc0/007/",
    "smallest/30074a0e/000/",
    "smallest/5a568359/000/",
    "smallest/95362737/009/",
    "smallest/48b82975/000/"
  )
  val runnable: List[String] = all filterNot (x => noPosSkip contains x)
}

object Syllables {
  private val all = List(
    "syllables/818f8cf4/007/",
    "syllables/38eb99ca/003/",
    "syllables/38eb99ca/004/",
    "syllables/36d8008b/003/",
    "syllables/f8d57dea/002/",
    "syllables/99cbb46b/003/",
    "syllables/48925325/007/",
    "syllables/2af3c416/003/",
    "syllables/fcf701e8/002/",
    "syllables/90a14c1a/000/",
    "syllables/ca505766/003/",
    "syllables/d5059e2b/000/",
    "syllables/b6fd408d/000/"
  )
  private val noPosSkip = List("syllables/48925325/007/")
  val runnable: List[String] = all filterNot (x => noPosSkip contains x)
}

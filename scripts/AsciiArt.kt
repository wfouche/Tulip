///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.leego:banana:2.1.0

import io.leego.banana.BananaUtils
import io.leego.banana.Font

fun main() {
   val text = "Tulip 2.0"
   val ascii = BananaUtils.bananaify(text, Font.STANDARD)
   println(ascii)
}


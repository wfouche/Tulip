@file:JvmName("LoadTest1")

import tulip.runTests
import tulip.getTest

fun main() {
    runTests(contexts, tests, ::getUser, ::getTest)
}
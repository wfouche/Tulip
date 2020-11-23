@file:JvmName("LoadTest1")

import tulip.runTests

fun getUser(userId: Int): User {
    return UserHttp(userId)
}

fun main() {
    runTests(contexts, tests, ::getUser)
}
/*-------------------------------------------------------------------------*/

@file:JvmName("LoadTest1")

/*-------------------------------------------------------------------------*/

import tulip.User
import tulip.runTests

/*-------------------------------------------------------------------------*/


fun getUser(userId: Int): User {
    return UserHttp(userId)
}

/*-------------------------------------------------------------------------*/

fun main() {
    runTests(contexts, tests, ::getUser)
}

/*-------------------------------------------------------------------------*/

// TODO: write a UI front-end for Tulip with Jetpack Compose - https://www.infoworld.com/article/3596322/jetbrains-releases-desktop-ui-framework-for-kotlin.html

/*-------------------------------------------------------------------------*/

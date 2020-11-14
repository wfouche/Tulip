/*-------------------------------------------------------------------------*/

@file:JvmName("LoadTest1")

/*-------------------------------------------------------------------------*/

import tulip.runTests

/*-------------------------------------------------------------------------*/

fun main() {
    runTests(contexts, tests, ::getUser, ::getTest)
}

/*-------------------------------------------------------------------------*/

// TODO: upgrade to Kotlin 1.4.20 from 1.4.20-RC

// TODO: write a UI front-end for Tulip with Jetpack Compose - https://www.infoworld.com/article/3596322/jetbrains-releases-desktop-ui-framework-for-kotlin.html

/*-------------------------------------------------------------------------*/

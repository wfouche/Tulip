/*-------------------------------------------------------------------------*/

@file:JvmName("LoadTest1")

/*-------------------------------------------------------------------------*/

import tulip.RuntimeConfig
import tulip.runTulip

/*-------------------------------------------------------------------------*/

fun main() {
    initTestSuite()
    val config = RuntimeConfig(
            NUM_USERS = NUM_USERS,
            NUM_THREADS = NUM_THREADS,
            testSuite = tests,
            newUser = ::newUser
    )
    runTulip(config)
}

/*-------------------------------------------------------------------------*/


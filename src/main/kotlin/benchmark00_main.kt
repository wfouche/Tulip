/*-------------------------------------------------------------------------*/

@file:JvmName("LoadTest1")

/*-------------------------------------------------------------------------*/

import tulip.RuntimeConfig
import tulip.runTulip
import tulip.initTulip

/*-------------------------------------------------------------------------*/

fun main() {
    initTestSuite()
    initTulip()
    for (tc in contexts) {
        val config = RuntimeConfig(
                name = tc.name,
                NUM_USERS = tc.numUsers,
                NUM_THREADS = tc.numThreads,
                testSuite = tests,
                newUser = ::newUser
        )
        runTulip(config)
    }
}

/*-------------------------------------------------------------------------*/


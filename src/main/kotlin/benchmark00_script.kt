/*-------------------------------------------------------------------------*/

import tulip.Action
import tulip.NUM_ACTIONS
import tulip.RuntimeContext
import tulip.TestProfile
import tulip.User

/*-------------------------------------------------------------------------*/

fun getUser(userId: Int): User {
    return UserHttp(userId)
}

/*-------------------------------------------------------------------------*/

fun getTest(context: RuntimeContext, testId: Int, test: TestProfile): TestProfile {
    //val tps =  when {
    //    c.numUsers == 4 && c.numThreads == 4 -> 100.0
    //    c.numUsers == 8 && c.numThreads == 4 -> 200.0
    //    else -> 0.0
    //}
    //return test.copy(arrivalRate = tps)
    //println("getTest: ${context.name}, ${testId}, ${test.filename}")
    return test
}

/*-------------------------------------------------------------------------*/

var contexts: List<RuntimeContext> = listOf(
        // Context 1
        RuntimeContext("Scenario-1", 4, 4, ::getTest),

        // Context 2
        RuntimeContext("Scenario-2", 8, 8, ::getTest)
)

/*-------------------------------------------------------------------------*/

const val JSON_FILENAME = "json_results.txt"

/*-------------------------------------------------------------------------*/

val tests: List<TestProfile> = listOf(

        // 0
        TestProfile(
                name = "Test0 (Initialize)",
                arrivalRate = 0.0,
                userProfile = listOf(1),
                actions = listOf(Action(0), Action(7)),
                filename = JSON_FILENAME
        ),

        // 1
        TestProfile(
                // The name of this test.
                name = "Test1 (Throughput Test - Max)",

                // Duration in minutes
                startupDurationMinutes = 1,
                warmupDurationMinutes = 1,
                mainDurationMinutes = 5,

                // Limit throughput 100.0 actions per second (on average).
                // A value of zero indicates that the arrival rate is uncapped.
                // λ value from Little's Law
                arrivalRate = 0.0,

                // Limit the number of active user objects, A value of
                // zero sets the number of active users to unlimited.
                // L value from Little's Law.
                userProfile = listOf(0, 2, 1),

                // Actions to be performed on the user objects during this test.
                actions = listOf(Action(8)),

                repeatCount = 1,

                filename = JSON_FILENAME
        ),

        // 2
        TestProfile(
                // The name of this test.
                name = "Test2 (Throughput Test - Fixed)",

                // Duration in minutes
                warmupDurationMinutes = 1,
                mainDurationMinutes = 1,

                // Limit throughput 100.0 actions per second (on average).
                // A value of zero indicates that the arrival rate is uncapped.
                // λ value from Little's Law
                arrivalRate = 100.0,

                // Limit the number of active user objects, A value of
                // zero sets the number of active users to unlimited.
                // L value from Little's Law.
                userProfile = listOf(0),

                // Actions to be performed on the user objects during this test.
                // 100 actions in total with a 50%/50% split between
                // action 0 and action 1
                // 6*1.0/4.0 + 14*3.0/4.0 = 12.0 ms (expected global average response time)
                actions = listOf(Action(1, 25), Action(2, 75)),

                repeatCount = 3,

                filename = JSON_FILENAME
        ),

        // 3
        TestProfile(
                name = "Test4 (Terminate)",
                arrivalRate = 5.0,
                userProfile = listOf(1),
                actions = listOf(Action(NUM_ACTIONS - 1)),
                filename = JSON_FILENAME
        )
)

/*-------------------------------------------------------------------------*/
